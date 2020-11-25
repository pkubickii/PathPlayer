package pl.pkubicki.controllers;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.util.LengthUnit;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class FreeTravelController implements Initializable {
    private final File owlFile= new File("C:\\Users\\pkubicki\\IntelliJIDEAProjects\\PathPlayer\\src\\main\\java\\pl\\pkubicki\\CityOnto.owl");
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
    }

    @FXML
    public void createProximityList(ActionEvent actionEvent) {
            LatLng poi = new LatLng(Double.parseDouble(latitudeText.getText()), Double.parseDouble(longitudeText.getText()));
            LengthUnit lengthUnit = (LengthUnit) unitChoiceBox.getValue();
            proximityListText.clear();
            proximityListText.setText((OwlUtils.createProximityNodeSet(poi, Double.parseDouble(proximityText.getText()), lengthUnit, OwlUtils.createNodeSetWithGps(reasoner, dataFactory), reasoner, dataFactory)).toString());
    }

}
