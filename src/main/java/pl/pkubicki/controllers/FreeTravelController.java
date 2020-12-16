package pl.pkubicki.controllers;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;
import fr.dudie.nominatim.client.JsonNominatimClient;
import fr.dudie.nominatim.model.Address;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.StringConverter;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import pl.pkubicki.util.FxUtils;
import pl.pkubicki.util.MediaUtils;
import pl.pkubicki.util.OwlUtils;
import pl.pkubicki.util.S3Utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;


public class FreeTravelController implements Initializable {
    @FXML private ChoiceBox unitChoiceBox;
    @FXML private ListView proximityListView;
    @FXML private TextArea currentLocationText;
    @FXML private TextField latitudeText;
    @FXML private TextField longitudeText;
    @FXML private TextField proximityText;
    @FXML private ChoiceBox<Address> searchResultsChoiceBox;
    @FXML private TextField startPoiText;
    @FXML private Button searchButton;
    @FXML private TextField searchText;
    @FXML private Button buttonN;
    @FXML private Button buttonNE;
    @FXML private Button buttonNW;
    @FXML private Button buttonS;
    @FXML private Button buttonSE;
    @FXML private Button buttonSW;
    @FXML private Button buttonE;
    @FXML private Button buttonW;
    @FXML private ChoiceBox stepLengthChoiceBox;
    @FXML private ChoiceBox vicinityDistChoiceBox;

    private static JsonNominatimClient nominatimClient;
    private static Properties PROPS = new Properties();
    private static final String PROPS_PATH = "src/main/resources/pl/pkubicki/properties/nominatim.properties";
    private static HttpClient httpClient;

    private static LatLng startPoi = null;
    private static LatLng nextPoi = null;
    private static double vicinity = 200.0;
    private static double stepLength = 10.0;

    private static ObservableList obListForStepLengths = FXCollections.emptyObservableList();
    private static ObservableMap<OWLNamedIndividual, String> individualsWithLabels = FXCollections.emptyObservableMap();
    private static ObservableList obListForUnitTypes = FXCollections.emptyObservableList();
    private static ObservableList obListForVicinityDistances = FXCollections.emptyObservableList();
    private static Set<OWLNamedIndividual> namedIndividualsInProximity = new HashSet<>();
    private static LinkedList<Media> audioList = new LinkedList<>();
    private static ObservableList<Media> observableAudioList = FXCollections.emptyObservableList();
    private static Media media;
    private static MediaPlayer player;
    private static boolean isPaused = false;
    private static boolean isStopped = true;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        latitudeText.setText("52.162995");
        longitudeText.setText("22.271528");
        proximityText.setText("100.0");

        initializeNominatimClient();
        initializeUnitTypeChoiceBox();
        initializeStepLengthChoiceBox();
        initializeStepLengthChoiceBoxListener();
        initializeVicinityDistances();
        initializeVicinityDistancesListenerToRefreshProximityValues();
        initializeSearchResultsListenerToRefreshGpsValues();
        initializeTravelButtonsHandler();
    }

    private void initializeNominatimClient() {
        try {
            InputStream in = new FileInputStream(PROPS_PATH);
            PROPS.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        ClientConnectionManager connexionManager = new SingleClientConnManager(null, registry);
        httpClient = new DefaultHttpClient(connexionManager, null);
        String baseUrl = PROPS.getProperty("nominatim.server.url");
        String email = PROPS.getProperty("nominatim.headerEmail");
        nominatimClient = new JsonNominatimClient(baseUrl, httpClient, email);
    }

    private void initializeUnitTypeChoiceBox() {
        List<LengthUnit> unitTypes = new ArrayList<LengthUnit>() {
            {
                add(LengthUnit.METER);
                add(LengthUnit.KILOMETER);
                add(LengthUnit.MILE);
                add(LengthUnit.NAUTICAL_MILE);
                add(LengthUnit.ROD);
            }
        };
        obListForUnitTypes = FXCollections.observableList(unitTypes);
        unitChoiceBox.getItems().clear();
        unitChoiceBox.setItems(obListForUnitTypes);
        unitChoiceBox.setValue(LengthUnit.METER);
    }

    private void initializeStepLengthChoiceBoxListener() {
        stepLengthChoiceBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    stepLength = Double.parseDouble(newVal.toString());
                    System.out.println(stepLength);
                }
        });
    }

    private void initializeStepLengthChoiceBox() {
        List<Double> stepLengths = new ArrayList<Double>() {
            {
                add(1.0);
                add(5.0);
                add(10.0);
                add(20.0);
                add(50.0);
                add(100.0);
            }
        };
        obListForStepLengths = FXCollections.observableList(stepLengths);
        stepLengthChoiceBox.getItems().clear();
        stepLengthChoiceBox.setItems(obListForStepLengths);
        stepLengthChoiceBox.setValue(10.0);
    }

    private void initializeVicinityDistancesListenerToRefreshProximityValues() {
        vicinityDistChoiceBox.valueProperty().addListener( (obs, oldVal, newVal) -> {
            if (newVal != null) {
               vicinity = Double.parseDouble(newVal.toString());
               if (startPoi != null) {
                   refreshProximityList(startPoi);
               } else {
                   System.out.println("No starting point.");
               }
                System.out.println(vicinity);
            }
        });
    }

    private void initializeVicinityDistances() {
        List<Double> vicinityDistances = new ArrayList<Double> () {
            {
                add(10.0);
                add(20.0);
                add(30.0);
                add(40.0);
                add(50.0);
                add(100.0);
                add(200.0);
            }
        };
        obListForVicinityDistances = FXCollections.observableList(vicinityDistances);
        vicinityDistChoiceBox.getItems().clear();
        vicinityDistChoiceBox.setItems(obListForVicinityDistances);
        vicinityDistChoiceBox.setValue(200.0);
    }

    private void initializeSearchResultsListenerToRefreshGpsValues() {
        searchResultsChoiceBox.valueProperty().addListener( (obs, oldVal, newVal) -> {
            if (newVal != null) {
                latitudeText.setText(String.valueOf(newVal.getLatitude()));
                longitudeText.setText(String.valueOf(newVal.getLongitude()));
            }
        });
    }

    private void initializeTravelButtonsHandler() {
        buttonN.setOnAction(new TravelButtonsHandler(LatLngTool.Bearing.NORTH));
        buttonNE.setOnAction(new TravelButtonsHandler(LatLngTool.Bearing.NORTH_EAST));
        buttonNW.setOnAction(new TravelButtonsHandler(LatLngTool.Bearing.NORTH_WEST));
        buttonS.setOnAction(new TravelButtonsHandler(LatLngTool.Bearing.SOUTH));
        buttonSE.setOnAction(new TravelButtonsHandler(LatLngTool.Bearing.SOUTH_EAST));
        buttonSW.setOnAction(new TravelButtonsHandler(LatLngTool.Bearing.SOUTH_WEST));
        buttonE.setOnAction(new TravelButtonsHandler(LatLngTool.Bearing.EAST));
        buttonW.setOnAction(new TravelButtonsHandler(LatLngTool.Bearing.WEST));
    }

    @FXML
    public void createProximityListWithLabels(ActionEvent actionEvent) {
        LatLng poi = new LatLng(Double.parseDouble(latitudeText.getText()), Double.parseDouble(longitudeText.getText()));
        double proximityDistance = Double.parseDouble(proximityText.getText());
        refreshProximityListView(poi, proximityDistance);
    }

    private void refreshProximityList(LatLng poi) {
        double proximityDistance = (Double) vicinityDistChoiceBox.getValue();
        refreshProximityListView(poi, proximityDistance);
    }

    private void refreshProximityListView(LatLng poi, double proximityDistance) {
        LengthUnit lengthUnit = (LengthUnit) unitChoiceBox.getValue();
        namedIndividualsInProximity = OwlUtils.getIndividualsInProximity(poi, proximityDistance, lengthUnit);
        Map<OWLNamedIndividual, String> points = OwlUtils.getIndividualsWithLabels(namedIndividualsInProximity);
        individualsWithLabels = FXCollections.observableMap(points);
        proximityListView.getItems().setAll(individualsWithLabels.values());
        proximityListView.getSelectionModel().selectFirst();
        refreshAudioList();
    }

    @FXML
    public void searchButtonHandler(ActionEvent actionEvent) throws IOException {
        if (!searchText.getText().isEmpty()) {
            FxUtils.generateSearchResultsInChBox(searchResultsChoiceBox, searchText.getText());
        } else {
            System.out.println("Search query is empty.");
        }
    }

    @FXML
    public void makeStartPoiFromGps(ActionEvent actionEvent) throws IOException {
        if (!latitudeText.getText().isEmpty() && !longitudeText.getText().isEmpty()) {
            Address address = nominatimClient.getAddress(Double.parseDouble(longitudeText.getText()), Double.parseDouble(latitudeText.getText()));
            startPoiText.setText(address.getDisplayName());
            startPoi = new LatLng(address.getLatitude(), address.getLongitude());
        } else {
            System.out.println("Empty gps coords.");
        }
    }

    private void refreshCurrentLocation() throws IOException {
        Address address = nominatimClient.getAddress(startPoi.getLongitude(), startPoi.getLatitude());
        currentLocationText.setText(address.getDisplayName());
    }
    private void refreshAudioList() {
        if (!proximityListView.getItems().isEmpty()) {
            audioList.clear();
            Map<OWLNamedIndividual, String> audioFileNames = OwlUtils.getAudioFileNames(namedIndividualsInProximity);
            audioFileNames.forEach( (k , v) -> {
                audioList.add(new Media((S3Utils.downloadFile(v)).toURI().toString()));
            });
        } else {
            System.out.println("Nothing to play.");
        }
        observableAudioList = FXCollections.observableList(audioList);
    }

    @FXML
    public void play(ActionEvent actionEvent) {
        if (!observableAudioList.isEmpty()) {
            MediaUtils.play(observableAudioList);
        } else {
            System.out.println("Nothing to play.");
        }
    }
    @FXML
    public void playAudio(ActionEvent actionEvent) {
        proximityListView.getSelectionModel().selectFirst();
        isStopped = false;
        this.playMediaList();

    }
    private void playMediaList() {
        if (!observableAudioList.isEmpty()) {
            media = observableAudioList.get(0);
            observableAudioList.remove(0);
            player = new MediaPlayer(media);
            player.setOnEndOfMedia(() -> {
                proximityListView.getSelectionModel().selectNext();
                this.playMediaList();
            });
            player.play();
        } else {
            System.out.println("Nothing to play.");
        }
    }

    @FXML
    public void pauseAudio(ActionEvent actionEvent) {
        if((player != null) && !(player.getStatus() == MediaPlayer.Status.DISPOSED) && !isStopped) {
            if(!isPaused) {
                player.pause();
                isPaused = true;
            } else {
                player.play();
                isPaused = false;
            }
        }
    }

    @FXML
    public void stopAudio(ActionEvent actionEvent) {
        if((player != null) && !(player.getStatus() == MediaPlayer.Status.DISPOSED)) {
            player.stop();
            isStopped = true;
        }
    }

    private class TravelButtonsHandler implements EventHandler<ActionEvent> {
        private double bearing;
        private TravelButtonsHandler() {
            this.bearing = LatLngTool.Bearing.NORTH;
        }
        private TravelButtonsHandler(double bearing) {
            this.bearing = bearing;
        }
        @Override
        public void handle(ActionEvent event) {
            if (startPoi != null) {
                nextPoi = LatLngTool.travel(startPoi, this.bearing, stepLength, LengthUnit.METER);
                refreshProximityList(nextPoi);
                try {
                    refreshCurrentLocation();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                startPoi = nextPoi;
                System.out.println("NEXT POI: " + nextPoi.toString());
            } else {
                System.out.println("No starting point.");
            }
        }
    }
}
