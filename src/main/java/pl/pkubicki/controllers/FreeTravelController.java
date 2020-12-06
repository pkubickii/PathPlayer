package pl.pkubicki.controllers;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;
import fr.dudie.nominatim.client.JsonNominatimClient;
import fr.dudie.nominatim.model.Address;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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
    private OWLReasoner reasoner;
    private OWLOntology ontology;

    private static Region region = Region.EU_NORTH_1;

    @FXML private ChoiceBox unitChoiceBox;
    @FXML private TextArea proximityListText;
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
    private static String PROPS_PATH = "src/main/resources/pl/pkubicki/properties/nominatim.properties";
    private static HttpClient httpClient;

    private static LatLng startPoi = null;
    private static LatLng nextPoi = null;
    private static double vicinity = 200.0;
    private static double stepLength = 10.0;

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
        ObservableList obListForUnitTypes = FXCollections.observableList(unitTypes);
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
        ObservableList obListForStepLengths = FXCollections.observableList(stepLengths);
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
        ObservableList obListForVicinityDistances = FXCollections.observableList(vicinityDistances);
        vicinityDistChoiceBox.getItems().clear();
        vicinityDistChoiceBox.setItems(obListForVicinityDistances);
        vicinityDistChoiceBox.setValue(200.0);
        vicinityDistChoiceBox.valueProperty().addListener( (obs, oldVal, newVal) -> {
            if (newVal != null) {
               vicinity = Double.parseDouble(newVal.toString());
               refreshProximityList(startPoi);
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
    public void createProximityList(ActionEvent actionEvent) {

            LatLng poi = new LatLng(Double.parseDouble(latitudeText.getText()), Double.parseDouble(longitudeText.getText()));
            LengthUnit lengthUnit = (LengthUnit) unitChoiceBox.getValue();
            proximityListText.clear();
            proximityListText.setText(OwlUtils.proximitySetToString(OwlUtils.createProximityNodeSet(poi, Double.parseDouble(proximityText.getText()), lengthUnit, reasoner, dataFactory)));
    }
    public void refreshProximityList(LatLng poi) {
        LengthUnit lengthUnit = (LengthUnit) unitChoiceBox.getValue();
        double vicinity = (Double) vicinityDistChoiceBox.getValue();

        proximityListText.setText(OwlUtils.proximitySetToString((OwlUtils.createProximityNodeSet(poi, vicinity, LengthUnit.METER, reasoner, dataFactory))));

    }

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
    public void makeStartPoiFromGps() throws IOException {
        if (!latitudeText.getText().isEmpty() && !longitudeText.getText().isEmpty()) {
            Address address = nominatimClient.getAddress(Double.parseDouble(longitudeText.getText()), Double.parseDouble(latitudeText.getText()));
            startPoiText.setText(address.getDisplayName());
            startPoi = new LatLng(address.getLatitude(), address.getLongitude());
        } else {
            System.out.println("Empty gps coords.");
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
                nextPoi = LatLngTool.travel(startPoi, this.bearing, 10.0, LengthUnit.METER);
                refreshProximityList(nextPoi);
                startPoi = nextPoi;
                System.out.println("NEXT POI: " + nextPoi.toString());
            } else {
                System.out.println("No starting point.");
            }
        }
    }
}
