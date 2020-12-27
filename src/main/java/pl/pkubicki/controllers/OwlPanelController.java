package pl.pkubicki.controllers;

import fr.dudie.nominatim.model.Address;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.semanticweb.owlapi.model.OWLClass;
import pl.pkubicki.models.OWLAudioTrack;
import pl.pkubicki.models.OWLPoint;
import pl.pkubicki.models.ValidatedTextField;
import pl.pkubicki.repositories.S3Repo;
import pl.pkubicki.util.FxUtils;
import pl.pkubicki.util.OwlManagement;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class OwlPanelController implements Initializable {
    @FXML private Label labelForSearchText;
    @FXML private TextField searchText;
    @FXML private Label labelForSearchResultsChoiceBox;
    @FXML private ChoiceBox<Address> searchResultsChoiceBox;
    @FXML private Label labelForLatitudeText;
    @FXML private ValidatedTextField latitudeText;
    @FXML private Label labelForLongitudeText;
    @FXML private ValidatedTextField longitudeText;
    @FXML private Label labelForLabelText;
    @FXML private ValidatedTextField labelText;
    @FXML private Label labelForCommentText;
    @FXML private TextArea commentText;
    @FXML private Label labelForRealEstatesChoiceBox;
    @FXML private ChoiceBox<OWLClass> realEstatesChoiceBox;

    @FXML private Text submitStatus;
    @FXML private Text fileName;
    @FXML private Button searchButton;
    @FXML private Button selectFileButton;
    @FXML private Button submitButton;

    private static File audioFile;
    private static ObservableList<OWLClass> realEstates = FXCollections.emptyObservableList();
    private static final Effect invalidEffect = new DropShadow(BlurType.GAUSSIAN, Color.RED, 9, 0.9, 2, 2);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        searchText.setOnKeyReleased(new FxUtils.SubmitTextFieldHandler(searchResultsChoiceBox, searchText));
        initializeRealEstatesChoiceBox();
        initializeSearchResultsListenerToRefreshGpsValues();
        initializeCommentText();

        initializeFocusListener(searchText, labelForSearchText);
        initializeFocusListener(searchButton);
        initializeFocusListener(searchResultsChoiceBox, labelForSearchResultsChoiceBox);
        initializeFocusListener(latitudeText, labelForLatitudeText);
        initializeFocusListener(longitudeText, labelForLongitudeText);
        initializeFocusListener(labelText, labelForLabelText);
        initializeFocusListener(realEstatesChoiceBox, labelForRealEstatesChoiceBox);
        initializeFocusListener(commentText, labelForCommentText);
        initializeFocusListener(commentText, labelForCommentText);
        initializeFocusListener(selectFileButton);
        initializeFocusListener(submitButton);
    }

    private void initializeFocusListener(Node focusedNode, Node nodeLabel) {
        FxUtils.getFocusListener(focusedNode, nodeLabel);
    }

    private void initializeFocusListener(Node focusedNode) {
        FxUtils.getFocusListener(focusedNode);
    }

    private void initializeCommentText() {
        commentText.addEventFilter(KeyEvent.KEY_PRESSED, new FxUtils.TabTraversalEventHandler());
        commentText.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) commentText.setEffect(null);
        });
    }

    private void initializeRealEstatesChoiceBox() {
        realEstates = FxUtils.getObRealEstateSubClasses();
        realEstatesChoiceBox.getItems().clear();
        realEstatesChoiceBox.setItems(realEstates);
        setRealEstatesChoiceBoxStringConverter(realEstatesChoiceBox);
        realEstatesChoiceBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) realEstatesChoiceBox.setEffect(null);
        });
    }
    private void initializeSearchResultsListenerToRefreshGpsValues() {
        searchResultsChoiceBox.valueProperty().addListener( (obs, oldVal, newVal) -> {
            if (newVal != null) {
                latitudeText.setText(String.valueOf(newVal.getLatitude()));
                longitudeText.setText(String.valueOf(newVal.getLongitude()));
            }
        });
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
    @FXML
    public void searchButtonHandler() {
        if (!searchText.getText().isEmpty()) {
            FxUtils.generateSearchResultsInChBox(searchResultsChoiceBox, searchText.getText());
        } else {
            System.out.println("Search query is empty.");
        }
    }

    @FXML
    public void submitHandler() {
        if(isFormValid()) {
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
            setErrorsOnInvalidFields();
            submitStatus.setText("Please check and correct highlighted fields in form.");
        }
    }

    private boolean isFormValid() {
        if(!latitudeText.getInvalid() &&
                !longitudeText.getInvalid() &&
                !labelText.getInvalid() &&
                !commentText.getText().isEmpty() &&
                !fileName.getText().isEmpty() &&
                realEstatesChoiceBox.getValue() != null) {
            return true;
        }
        return false;
    }

    private void setErrorsOnInvalidFields() {
        if (latitudeText.getInvalid()) latitudeText.setEffect(invalidEffect);
        if (longitudeText.getInvalid()) longitudeText.setEffect(invalidEffect);
        if (labelText.getInvalid()) labelText.setEffect(invalidEffect);
        if (commentText.getText().isEmpty()) commentText.setEffect(invalidEffect);
        if (fileName.getText().isEmpty()) selectFileButton.setEffect(invalidEffect);
        if (realEstatesChoiceBox.getValue() == null) realEstatesChoiceBox.setEffect(invalidEffect);
    }

    @FXML
    public void selectFile(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open file");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Audio files (*.mp3)", "*.mp3");
        fileChooser.getExtensionFilters().add(extFilter);
        audioFile = fileChooser.showOpenDialog(new Stage());
        if (audioFile != null) {
            fileName.setText(audioFile.getName());
        }
    }

}
