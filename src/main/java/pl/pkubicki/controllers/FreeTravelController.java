package pl.pkubicki.controllers;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;
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
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import pl.pkubicki.util.FxUtils;
import pl.pkubicki.util.MediaUtils;
import pl.pkubicki.util.NominatimUtils;
import pl.pkubicki.util.OwlUtils;

import java.io.IOException;
import java.net.URL;
import java.util.*;


public class FreeTravelController implements Initializable {
    @FXML private ChoiceBox<LengthUnit> unitChoiceBox;
    @FXML private ListView<String> proximityListView;
    @FXML private TextArea currentLocationText;
    @FXML private TextField latitudeText;
    @FXML private TextField longitudeText;
    @FXML private TextField proximityText;
    @FXML private ChoiceBox<Address> searchResultsChoiceBox;
    @FXML private TextField startPoiText;
    @FXML private TextField searchText;
    @FXML private Button buttonN;
    @FXML private Button buttonNE;
    @FXML private Button buttonNW;
    @FXML private Button buttonS;
    @FXML private Button buttonSE;
    @FXML private Button buttonSW;
    @FXML private Button buttonE;
    @FXML private Button buttonW;
    @FXML private ChoiceBox<Double> stepLengthChoiceBox;
    @FXML private ChoiceBox<Double> vicinityDistChoiceBox;

    private static LatLng startPoint = null;
    private static double vicinity = 200.0;
    private static double stepLength = 10.0;

    private static ObservableList<Double> obListForStepLengths = FXCollections.emptyObservableList();
    private static ObservableMap<OWLNamedIndividual, String> individualsWithLabels = FXCollections.emptyObservableMap();
    private static ObservableList<LengthUnit> obListForUnitTypes = FXCollections.emptyObservableList();
    private static ObservableList<Double> obListForVicinityDistances = FXCollections.emptyObservableList();
    private static Set<OWLNamedIndividual> namedIndividualsInProximity = new HashSet<>();
    private static LinkedList<Media> audioTracks = new LinkedList<>();
    private static Media media;
    private static MediaPlayer player;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        latitudeText.setText("52.162995");
        longitudeText.setText("22.271528");
        proximityText.setText("100.0");

        searchText.setOnKeyReleased(new FxUtils.SubmitTextFieldHandler(searchResultsChoiceBox, searchText));
        initializeUnitTypeChoiceBox();
        initializeStepLengthChoiceBox();
        initializeStepLengthChoiceBoxListener();
        initializeVicinityDistances();
        initializeVicinityDistancesListenerToRefreshProximityValues();
        initializeSearchResultsListenerToRefreshGpsValues();
        initializeTravelButtonsHandler();
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
               if (startPoint != null) {
                   refreshProximityList(startPoint);
               } else {
                   System.out.println("No starting point.");
               }
                System.out.println(vicinity);
            }
        });
    }

    private void initializeVicinityDistances() {
        obListForVicinityDistances = FxUtils.getObListForVicinity();
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
        double proximityDistance = vicinityDistChoiceBox.getValue();
        refreshProximityListView(poi, proximityDistance);
    }

    private void refreshProximityListView(LatLng poi, double proximityDistance) {
        LengthUnit lengthUnit = unitChoiceBox.getValue();
        namedIndividualsInProximity = OwlUtils.getIndividualsInPointProximity(poi, proximityDistance, lengthUnit);
        Map<OWLNamedIndividual, String> points = OwlUtils.getIndividualsWithLabels(namedIndividualsInProximity);
        individualsWithLabels = FXCollections.observableMap(points);
        proximityListView.getItems().setAll(individualsWithLabels.values());
        proximityListView.getSelectionModel().selectFirst();
    }

    @FXML
    public void searchButtonHandler(ActionEvent actionEvent) {
        if (!searchText.getText().isEmpty()) {
            FxUtils.generateSearchResultsInChBox(searchResultsChoiceBox, searchText.getText());
        } else {
            System.out.println("Search query is empty.");
        }
    }

    @FXML
    public void makeStartPointFromGps(ActionEvent actionEvent) throws IOException {
        if (!latitudeText.getText().isEmpty() && !longitudeText.getText().isEmpty()) {
            startPoint = new LatLng(Double.parseDouble(latitudeText.getText()), Double.parseDouble(longitudeText.getText()));
            startPoiText.setText(NominatimUtils.getCurrentLocationAddress(startPoint).getDisplayName());
        } else {
            System.out.println("Empty gps coords.");
        }
    }

    private void refreshCurrentLocation() throws IOException {
        if (startPoint != null) {
            Address address = NominatimUtils.getCurrentLocationAddress(startPoint);
            currentLocationText.setText(address.getDisplayName());
        } else {
            System.out.println("No Starting Point.");
        }
    }

    @FXML
    public void playAudio(ActionEvent actionEvent) {
        refreshAudioList();
        if (player != null && player.getStatus() == MediaPlayer.Status.PLAYING) return;
        if(!audioTracks.isEmpty()) {
            proximityListView.getSelectionModel().selectFirst();
            LinkedList<Media> tempAudioTracks = new LinkedList<>();
            Collections.addAll(tempAudioTracks, audioTracks.toArray(new Media[0]));
            playAudioTracks(tempAudioTracks);
        }
    }

    private void refreshAudioList() {
        if (!namedIndividualsInProximity.isEmpty()) {
            audioTracks = FxUtils.getAudioTracks(namedIndividualsInProximity);
        } else {
            audioTracks.clear();
            System.out.println("Nothing to play.");
        }
    }

    private void playAudioTracks(LinkedList<Media> audioList) {
        if (!audioList.isEmpty()) {
            media = audioList.poll();
            player = new MediaPlayer(media);
            player.setOnEndOfMedia(() -> {
                proximityListView.getSelectionModel().selectNext();
                playAudioTracks(audioList);
            });
            player.play();
        } else {
            System.out.println("No audio to play");
            player.dispose();
        }
    }

    @FXML
    public void pauseAudio(ActionEvent actionEvent) {
        MediaUtils.pause(player);
    }

    @FXML
    public void stopAudio(ActionEvent actionEvent) {
        MediaUtils.stop(player);
        audioTracks.clear();
    }

    private class TravelButtonsHandler implements EventHandler<ActionEvent> {
        private final double bearing;
        private TravelButtonsHandler() {
            this.bearing = LatLngTool.Bearing.NORTH;
        }
        private TravelButtonsHandler(double bearing) {
            this.bearing = bearing;
        }
        @Override
        public void handle(ActionEvent event) {
            if (startPoint != null) {
                LatLng nextPoint = LatLngTool.travel(startPoint, this.bearing, stepLength, LengthUnit.METER);
                refreshProximityList(nextPoint);
                try {
                    refreshCurrentLocation();
                } catch (IOException e) {
                    System.out.println("Nominatim error: " + e.getMessage() + "cause: " + e.getCause());
                    e.printStackTrace();
                }
                startPoint = nextPoint;
                System.out.println("NEXT POI: " + nextPoint.toString());
            } else {
                System.out.println("No starting point.");
            }
        }
    }
}
