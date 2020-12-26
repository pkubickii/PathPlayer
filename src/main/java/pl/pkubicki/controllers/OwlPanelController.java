package pl.pkubicki.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.semanticweb.owlapi.model.OWLClass;
import pl.pkubicki.models.OWLAudioTrack;
import pl.pkubicki.models.OWLPoint;
import pl.pkubicki.util.FxUtils;
import pl.pkubicki.util.OwlManagement;
import pl.pkubicki.repositories.S3Repo;
import software.amazon.awssdk.regions.Region;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;


public class OwlPanelController implements Initializable {
    private static Region region = Region.EU_NORTH_1;
    @FXML private TextField latitudeText;
    @FXML private TextField longitudeText;
    @FXML private TextField labelText;
    @FXML private ChoiceBox<OWLClass> realEstatesChoiceBox;
    @FXML private TextArea commentText;
    @FXML private Text submitStatus;
    @FXML private Text fileName;
    private static File audioFile;

    private static ObservableList<OWLClass> realEstates = FXCollections.emptyObservableList();

    @FXML
    public void selectFile(ActionEvent actionEvent){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open file");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Audio files (*.mp3)", "*.mp3");
        fileChooser.getExtensionFilters().add(extFilter);
        audioFile = fileChooser.showOpenDialog(new Stage());
        if (audioFile != null) {
            fileName.setText(audioFile.getName());
        }
    }

    @FXML
    public void submitHandler() {
        if(!latitudeText.getText().isEmpty() &&
                !longitudeText.getText().isEmpty() &&
                !labelText.getText().isEmpty() &&
                !commentText.getText().isEmpty() &&
                !fileName.getText().isEmpty() &&
                realEstatesChoiceBox.getValue() != null) {

            OWLPoint newPoint = new OWLPoint(
                    Double.parseDouble(latitudeText.getText()),
                    Double.parseDouble(longitudeText.getText()),
                    labelText.getText(),
                    commentText.getText(),
                    realEstatesChoiceBox.getValue());
            OWLAudioTrack newAudioTrack = new OWLAudioTrack(newPoint.getPointIndividual());
            OwlManagement.savePoint(newPoint);
            OwlManagement.saveAudioTrack(newAudioTrack);
            S3Repo.uploadFile(audioFile, newAudioTrack.getName() + ".mp3");
        } else {
            System.out.println("Empty fields in form.");
            submitStatus.setText("Empty fields in form.");
        }

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        latitudeText.setText("52.16354555");
        longitudeText.setText("22.27205220610704");
        labelText.setText("UPH - Dom Studenta Nr 1");
        commentText.setText("UPH - Dom Studenta Nr1 przy ulicy 3 Maja");
        initializeRealEstatesChoiceBox();
    }

    private void initializeRealEstatesChoiceBox() {
        realEstates = FxUtils.getObRealEstateSubClasses();
        realEstatesChoiceBox.getItems().clear();
        realEstatesChoiceBox.setItems(realEstates);
        setRealEstatesChoiceBoxStringConverter(realEstatesChoiceBox);
    }

    private void setRealEstatesChoiceBoxStringConverter(ChoiceBox<OWLClass> choiceBox) {
        choiceBox.setConverter(new StringConverter<OWLClass>() {
            @Override
            public String toString(OWLClass object) {
                return object.getIRI().getRemainder().get();
            }

            @Override
            public OWLClass fromString(String string) {
                return choiceBox.getItems().stream().filter(owlClass ->
                        owlClass.getIRI().getRemainder().get().equals(string)).findFirst().orElse(null);
            }
        });
    }
}
