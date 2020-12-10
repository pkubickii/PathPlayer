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
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.StringConverter;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import pl.pkubicki.util.OwlUtils;
import software.amazon.awssdk.regions.Region;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;


public class FreeTravelController implements Initializable {
    private final File owlFile= new File("src/main/java/pl/pkubicki/CityOnto.owl");
    private final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    private final OWLDataFactory dataFactory = manager.getOWLDataFactory();
    private final OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
    private static OWLReasoner reasoner;
    private static OWLOntology ontology;

    private static final Region region = Region.EU_NORTH_1;

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
    private static ObservableMap<OWLNamedIndividual, String> observableMap = FXCollections.emptyObservableMap();
    private static ObservableList obListForUnitTypes = FXCollections.emptyObservableList();
    private static ObservableList obListForVicinityDistances = FXCollections.emptyObservableList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configuration for Nominatim Client
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

        // Fixed GPS point for testing purposes only
        latitudeText.setText("52.162995");
        longitudeText.setText("22.271528");

        //currentLocationText.setWrapText(true);

        // Initialization for ontology manager and reasoner
        try {
            ontology = manager.loadOntologyFromOntologyDocument(owlFile);
            reasoner = reasonerFactory.createReasoner(ontology);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }

        // Configuration for unit type choice box
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
        proximityText.setText("100.0");
        unitChoiceBox.setValue(LengthUnit.METER);

        // Configuration + listener for step length choice box
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
        stepLengthChoiceBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    stepLength = Double.parseDouble(newVal.toString());
                    System.out.println(stepLength);
                }
        });

        // Configuration + listener for vicinity choice box
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
        vicinityDistChoiceBox.valueProperty().addListener( (obs, oldVal, newVal) -> {
            if (newVal != null) {
               vicinity = Double.parseDouble(newVal.toString());
               if (startPoi != null) {
                   refreshProximityList(startPoi);
                try {
                    refreshCurrentLocation();
                } catch (IOException e) {
                    e.printStackTrace();
                }
               } else {
                   System.out.println("No starting point.");
               }
                System.out.println(vicinity);
            }
        });

        // Listener which change gps values to point picked from search results
        searchResultsChoiceBox.valueProperty().addListener( (obs, oldVal, newVal) -> {
            if (newVal != null) {
                latitudeText.setText(String.valueOf(newVal.getLatitude()));
                longitudeText.setText(String.valueOf(newVal.getLongitude()));
            }
        });

        // Initialization of travel buttons listeners
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
        LengthUnit lengthUnit = (LengthUnit) unitChoiceBox.getValue();
        double proximityDistance = Double.parseDouble(proximityText.getText());
        Map<OWLNamedIndividual, String> points = OwlUtils.individualsLabels((OwlUtils.createProximityNodeSet(poi, proximityDistance, lengthUnit, reasoner, dataFactory)),ontology);
        observableMap = FXCollections.observableMap(points);
        proximityListView.getItems().setAll(observableMap.values());
        proximityListView.getSelectionModel().selectFirst();
    }

    private void refreshProximityList(LatLng poi) {
        LengthUnit lengthUnit = (LengthUnit) unitChoiceBox.getValue();
        double vicinity = (Double) vicinityDistChoiceBox.getValue();
        Map<OWLNamedIndividual, String> points = OwlUtils.individualsLabels((OwlUtils.createProximityNodeSet(poi, vicinity, lengthUnit, reasoner, dataFactory)),ontology);
        observableMap = FXCollections.observableMap(points);
        proximityListView.getItems().setAll(observableMap.values());
        proximityListView.getSelectionModel().selectFirst();
    }

    @FXML
    public void createGeoLocation(ActionEvent actionEvent) throws IOException {
        if (!searchText.getText().isEmpty()) {
            List<Address> addresses = nominatimClient.search(searchText.getText());
            ObservableList<Address> observableList = FXCollections.observableList(addresses);
            searchResultsChoiceBox.getItems().clear();
            searchResultsChoiceBox.setItems(observableList);
            searchResultsChoiceBox.setConverter(new StringConverter<Address>() {
                @Override
                public String toString(Address object) {
                    return object.getDisplayName();
                }

                @Override
                public Address fromString(String string) {
                    return searchResultsChoiceBox.getItems().stream().filter(address ->
                            address.getDisplayName().equals(string)).findFirst().orElse(null);
                }
            });
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
    @FXML
    public void playAudio(ActionEvent actionEvent) {
        if (!proximityListView.getItems().isEmpty()) {

        } else {
            System.out.println("Nothing to play.");
        }
    }

    @FXML
    public void pauseAudio(ActionEvent actionEvent) {

    }

    @FXML
    public void stopAudio(ActionEvent actionEvent) {

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
