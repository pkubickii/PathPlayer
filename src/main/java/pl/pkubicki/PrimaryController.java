package pl.pkubicki;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.FunctionalSyntaxDocumentFormat;
import org.semanticweb.owlapi.model.*;

import static pl.pkubicki.App.loadFXML;

public class PrimaryController {
    private OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }
    @FXML
    private void showOwl(ActionEvent actionEvent) {
        File file = new File("C:\\Users\\pkubicki\\IntelliJIDEAProjects\\PathPlayer\\src\\main\\java\\pl\\pkubicki\\CityOnto.owl");
        try {
            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);
            System.out.println("Test buttona." + manager.getOntologies().size());
            System.out.println(ontology);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void saveOwl(ActionEvent actionEvent) {
        File fileOut = new File("C:\\Users\\pkubicki\\IntelliJIDEAProjects\\PathPlayer\\src\\main\\java\\pl\\pkubicki\\CityOntoSave.owl");
        File file = new File("C:\\Users\\pkubicki\\IntelliJIDEAProjects\\PathPlayer\\src\\main\\java\\pl\\pkubicki\\CityOnto.owl");
        try {
            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);
            manager.saveOntology(ontology, new FunctionalSyntaxDocumentFormat(), new FileOutputStream(fileOut));
        } catch (OWLOntologyCreationException | FileNotFoundException | OWLOntologyStorageException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void loadTestOWL(ActionEvent actionEvent) {
        File fileOut = new File("C:\\Users\\pkubicki\\IntelliJIDEAProjects\\PathPlayer\\src\\main\\java\\pl\\pkubicki\\TestOnto.owl");
        IRI testOnto = IRI.create("http://www.cs.man.ac.uk/~stevensr/ontology/family.rdf.owl");
        OWLOntology familyOnto = null;
        try {
            familyOnto = manager.loadOntology(testOnto);
            manager.saveOntology(familyOnto, new FunctionalSyntaxDocumentFormat(), new FileOutputStream(fileOut));
        } catch (OWLOntologyCreationException | FileNotFoundException | OWLOntologyStorageException e) {
            e.printStackTrace();
        }
        System.out.println(familyOnto);
    }
    @FXML
    private void createEmptyOWL(ActionEvent actionEvent) {
        File fileOut = new File ("C:\\Users\\pkubicki\\IntelliJIDEAProjects\\PathPlayer\\src\\main\\java\\pl\\pkubicki\\EmptyOnto.owl");
        OWLOntology o;
        try {
            o = manager.createOntology();
            System.out.println(o);
            manager.saveOntology(o, new FunctionalSyntaxDocumentFormat(), new FileOutputStream(fileOut));
        } catch (OWLOntologyCreationException | FileNotFoundException | OWLOntologyStorageException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void openUploadForm(ActionEvent actionEvent) throws IOException{
        Stage stage = new Stage();
        stage.setTitle("New POI form");
        stage.setScene(new Scene(loadFXML("addpoiform"), 640, 480));
        stage.show();
    }
}
