package pl.pkubicki.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.FunctionalSyntaxDocumentFormat;
import org.semanticweb.owlapi.model.*;
import pl.pkubicki.App;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static pl.pkubicki.App.loadFXML;

public class PrimaryController {
    private final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("fxml/secondary");
    }
    @FXML
    private void showOwl(ActionEvent actionEvent) {
        File file = new File("src/main/java/pl/pkubicki/CityOnto.owl");
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
        File fileOut = new File("src/main/java/pl/pkubicki/CityOntoSave.owl");
        File file = new File("src/main/java/pl/pkubicki/CityOnto.owl");
        try {
            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(file);
            manager.saveOntology(ontology, new FunctionalSyntaxDocumentFormat(), new FileOutputStream(fileOut));
        } catch (OWLOntologyCreationException | FileNotFoundException | OWLOntologyStorageException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void loadTestOWL(ActionEvent actionEvent) {
        File fileOut = new File("src/main/java/pl/pkubicki/TestOnto.owl");
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
        File fileOut = new File ("src/main/java/pl/pkubicki/CityOnto.owl");
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
    private void openUploadForm(ActionEvent actionEvent) throws IOException {
        Stage stage = new Stage();
        stage.setTitle("New POI form");
        stage.setScene(new Scene(loadFXML("fxml/addpoiform"), 640, 480));
        stage.show();
    }
    @FXML
    private void openDownloadForm(ActionEvent actionEvent) throws IOException {
        Stage stage = new Stage();
        stage.setTitle("Download POI form");
        stage.setScene(new Scene(loadFXML("fxml/downloadpoi"), 640, 480));
        stage.show();
    }

    public void openFreeTravelWindow(ActionEvent actionEvent) throws IOException {
        Stage stage = new Stage();
        stage.setTitle("Free Travel");
        stage.setScene(new Scene(loadFXML("fxml/freetravel"), 640, 480));
        stage.show();
    }
}
