package pl.pkubicki.controllers;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.util.LengthUnit;
import fr.dudie.nominatim.client.JsonNominatimClient;
import fr.dudie.nominatim.model.Address;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;


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

    private static JsonNominatimClient nominatimClient;
    private static Properties PROPS = new Properties();
    private static String PROPS_PATH = "src/main/resources/pl/pkubicki/properties/nominatim.properties";
    private static HttpClient httpClient;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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

        latitudeText.setText("52.162995");
        longitudeText.setText("22.271528");

        try {
            ontology = manager.loadOntologyFromOntologyDocument(owlFile);
            reasoner = reasonerFactory.createReasoner(ontology);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
        List<LengthUnit> lengthUnitList = new ArrayList<LengthUnit>() {
            {
                add(LengthUnit.METER);
                add(LengthUnit.KILOMETER);
                add(LengthUnit.MILE);
                add(LengthUnit.NAUTICAL_MILE);
                add(LengthUnit.ROD);
            }
        };
        ObservableList obList = FXCollections.observableList(lengthUnitList);
        unitChoiceBox.getItems().clear();
        unitChoiceBox.setItems(obList);
        proximityText.setText("100.0");
        unitChoiceBox.setValue(LengthUnit.METER);

        searchResultsChoiceBox.valueProperty().addListener( (obs, oldVal, newVal) -> {
            if (newVal != null) {
                latitudeText.setText(String.valueOf(newVal.getLatitude()));
                longitudeText.setText(String.valueOf(newVal.getLongitude()));
            }
        });


    }

    @FXML
    public void createProximityList(ActionEvent actionEvent) {

            LatLng poi = new LatLng(Double.parseDouble(latitudeText.getText()), Double.parseDouble(longitudeText.getText()));
            LengthUnit lengthUnit = (LengthUnit) unitChoiceBox.getValue();
            proximityListText.clear();
            proximityListText.setText((OwlUtils.createProximityNodeSet(poi, Double.parseDouble(proximityText.getText()), lengthUnit, OwlUtils.createNodeSetWithGps(reasoner, dataFactory), reasoner, dataFactory)).toString());
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
        } else {
            System.out.println("Empty gps coords.");
        }
    }
    public void fillGpsCoords(ActionEvent actionEvent) {
//        if (searchResultsChoiceBox.getValue() != null) {
//            Address address = (Address) searchResultsChoiceBox.getValue();
//            latitudeText.setText(String.valueOf(address.getLatitude()));
//            longitudeText.setText(String.valueOf(address.getLongitude()));
//        } else {
//            System.out.println("Empty search results.");
//        }
    }
}
