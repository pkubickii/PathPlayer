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
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import pl.pkubicki.extensions.ValidatedTextField;
import pl.pkubicki.util.*;

import java.io.IOException;
import java.net.URL;
import java.util.*;


public class FreeTravelController implements Initializable {

    @FXML private Label labelForSearchText;
    @FXML private TextField searchText;
    @FXML private Label labelForSearchResults;
    @FXML private ChoiceBox<Address> searchResultsChoiceBox;
    @FXML private Label labelForLatitudeText;
    @FXML private ValidatedTextField latitudeText;
    @FXML private Label labelForLongitudeText;
    @FXML private ValidatedTextField longitudeText;
    @FXML private Label labelForProximityText;
    @FXML private ValidatedTextField proximityText;
    @FXML private Label labelForStartPointText;
    @FXML private TextField startPointText;
    @FXML private Label labelForCurrentLocationTextArea;
    @FXML private TextArea currentLocationText;
    @FXML private Label labelForStepLengthChoiceBox;
    @FXML private ChoiceBox<Double> stepLengthChoiceBox;
    @FXML private Label labelForVicinityChoiceBox;
    @FXML private ChoiceBox<Double> vicinityDistChoiceBox;
    @FXML private Label labelForPointsListView;
    @FXML private ListView<String> proximityListView;
    @FXML private ChoiceBox<LengthUnit> unitChoiceBox;

    @FXML private Button createStartPointButton;
    @FXML private Button searchButton;
    @FXML private Button createProximityPointsButton;
    @FXML private Button buttonN;
    @FXML private Button buttonNE;
    @FXML private Button buttonNW;
    @FXML private Button buttonS;
    @FXML private Button buttonSE;
    @FXML private Button buttonSW;
    @FXML private Button buttonE;
    @FXML private Button buttonW;
    @FXML private Button playButton;
    @FXML private Button pauseButton;
    @FXML private Button stopButton;

    private static LatLng startPoint = null;
    private static LengthUnit unitType = LengthUnit.METER;
    private static double vicinity = 50.0;
    private static double stepLength = 10.0;

    private static ObservableList<Double> obListForStepLengths = FXCollections.emptyObservableList();
    private static ObservableMap<OWLNamedIndividual, String> individualsWithLabels = FXCollections.emptyObservableMap();
    private static ObservableList<LengthUnit> obListForUnitTypes = FXCollections.emptyObservableList();
    private static ObservableList<Double> obListForVicinityDistances = FXCollections.emptyObservableList();
    private static Set<OWLNamedIndividual> namedIndividualsInProximity = new HashSet<>();
    private static LinkedList<Media> audioTracks = new LinkedList<>();
    private static Media media;
    private static MediaPlayer player;
    private static final Effect invalidEffect = new DropShadow(BlurType.GAUSSIAN, Color.RED, 9, 0.9, 2, 2);
    private static String currentLocationString = "";
    private static String startPointString = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        latitudeText.setText("52.162995");
        longitudeText.setText("22.271528");
        proximityText.setText("100.0");

        searchText.setOnKeyReleased(new FxUtils.SubmitTextFieldHandler(searchResultsChoiceBox, searchText));
        initializeUnitTypeChoiceBox();
        initializeStepLengthChoiceBox();
        initializeLengthUnitListenerToRefreshProximityValues();
        initializeVicinityDistancesChoiceBox();
        initializeSearchResultsListeners();
        initializeTravelButtonsHandler();

        initializeFocusListener(searchText, labelForSearchText, true);
        initializeFocusListener(searchResultsChoiceBox, labelForSearchResults, false);
        initializeFocusListener(latitudeText, labelForLatitudeText, true);
        initializeFocusListener(longitudeText, labelForLongitudeText, true);
        initializeFocusListener(proximityText, labelForProximityText, true);
        initializeFocusListener(startPointText, labelForStartPointText, true);
        initializeFocusListener(currentLocationText, labelForCurrentLocationTextArea, true);
        initializeFocusListener(stepLengthChoiceBox, labelForStepLengthChoiceBox, false);
        initializeFocusListener(vicinityDistChoiceBox, labelForVicinityChoiceBox, false);
        initializeFocusListener(proximityListView, labelForPointsListView, true);
        initializeFocusListener(unitChoiceBox, false);
        initializeStartPointTextFieldActionListener();
        initializeFocusListener(createStartPointButton, true);
        initializeFocusListener(searchButton, true);
        initializeFocusListener(createProximityPointsButton, true);
        initializeFocusListener(buttonN, true);
        initializeFocusListener(buttonNW, true);
        initializeFocusListener(buttonNE, true);
        initializeFocusListener(buttonW, true);
        initializeFocusListener(buttonE, true);
        initializeFocusListener(buttonS, true);
        initializeFocusListener(buttonSW, true);
        initializeFocusListener(buttonSE, true);
        initializeFocusListener(playButton, true);
        initializeFocusListener(pauseButton, true);
        initializeFocusListener(stopButton, true);

        currentLocationText.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) return;
                newScene.addEventFilter(KeyEvent.KEY_RELEASED, new VoiceCueEventHandler());
        });

        startPointText.setOnKeyReleased(event -> {
            KeyCode code = event.getCode();
            if(code == KeyCode.F2) {
                if (!startPointString.isEmpty())
                    new Thread(() -> PollyUtils.play(startPointString)).start();
                else System.out.println("No starting point.");
            }
        });

        initializeAudioHelpers();
    }



    private void initializeFocusListener(Node focusedNode, Node nodeLabel, boolean sound) {
        FxUtils.getFocusListener(focusedNode, nodeLabel, sound);
    }

    private void initializeFocusListener(Node focusedNode, boolean sound) {
        FxUtils.getFocusListener(focusedNode, sound);
    }

    private void initializeUnitTypeChoiceBox() {
        initializeUnitTypeChoiceBoxValues();
        initializeUnitTypeChoiceBoxListener();
        FxUtils.getChoiceBoxListenersForSound(unitChoiceBox);
    }
    private void initializeUnitTypeChoiceBoxValues() {
        obListForUnitTypes = FxUtils.getObListForLengthUnits();
        unitChoiceBox.getItems().clear();
        unitChoiceBox.setItems(obListForUnitTypes);
        unitChoiceBox.setValue(LengthUnit.METER);
    }

    private void initializeUnitTypeChoiceBoxListener() {
        unitChoiceBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                unitType = newVal;
            }
        });
    }

    private void initializeStepLengthChoiceBox() {
        initializeStepLengthChoiceBoxValues();
        initializeStepLengthChoiceBoxChangeListener();
        FxUtils.getChoiceBoxListenersForSound(stepLengthChoiceBox);
    }
    private void initializeStepLengthChoiceBoxValues() {
        obListForStepLengths = FxUtils.getObListForStepLength();
        stepLengthChoiceBox.getItems().clear();
        stepLengthChoiceBox.setItems(obListForStepLengths);
        stepLengthChoiceBox.setValue(10.0);
    }

    private void initializeStepLengthChoiceBoxChangeListener() {
        stepLengthChoiceBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    stepLength = newVal;
                }
        });
    }
    private void initializeVicinityDistancesChoiceBox() {
        initializeVicinityDistancesValues();
        initializeVicinityDistancesListenerToRefreshProximityValues();
        FxUtils.getChoiceBoxListenersForSound(vicinityDistChoiceBox);
    }

    private void initializeVicinityDistancesValues() {
        obListForVicinityDistances = FxUtils.getObListForVicinity();
        vicinityDistChoiceBox.getItems().clear();
        vicinityDistChoiceBox.setItems(obListForVicinityDistances);
        vicinityDistChoiceBox.setValue(50.0);
    }

    private void initializeVicinityDistancesListenerToRefreshProximityValues() {
        addChangeListenerToChoiceBox(vicinityDistChoiceBox);
    }

    private void initializeLengthUnitListenerToRefreshProximityValues() {
        addChangeListenerToChoiceBox(unitChoiceBox);
    }

    private void addChangeListenerToChoiceBox(ChoiceBox<?> cB) {
        cB.valueProperty().addListener( (obs, oldVal, newVal) -> {
            if (newVal != null) {
                if(cB.getValue() instanceof Double) vicinity = (Double) newVal;
                else if(cB.getValue() instanceof LengthUnit) unitType = (LengthUnit) newVal;
                else System.out.println("Wrong choice box to add listener.");
                if (startPoint != null) {
                    refreshProximityPoints(startPoint);
                } else if (!latitudeText.getInvalid() && !longitudeText.getInvalid()){
                    makeStartPointFromGps();
                    refreshProximityPoints(startPoint);
                }
            }
        });
    }

    private void initializeSearchResultsListeners() {
        FxUtils.updateGpsValuesFromSearchResultsChoiceBox(searchResultsChoiceBox, latitudeText, longitudeText);
        FxUtils.getChoiceBoxListenersForSound(searchResultsChoiceBox);
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
        buttonN.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) return;
            newScene.setOnKeyReleased(new NumPadTraversalHandler());
        });
    }

    private void initializeStartPointTextFieldActionListener() {
        startPointText.setOnKeyReleased(event -> {
            if(event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                makeStartPointFromGps();
            }
        });
    }

    private void initializeAudioHelpers() {
        latitudeText.setOnKeyReleased(new FxUtils.AudioHelpEventHandler("latitudeHelp"));
        longitudeText.setOnKeyReleased(new FxUtils.AudioHelpEventHandler("longitudeHelp"));
        searchText.setOnKeyReleased(new FxUtils.AudioHelpEventHandler("searchHelp"));
        searchButton.setOnKeyReleased(new FxUtils.AudioHelpEventHandler("searchButtonHelp"));
        searchResultsChoiceBox.setOnKeyReleased(new FxUtils.AudioHelpEventHandler("searchResultsHelp"));
        proximityText.setOnKeyReleased(new FxUtils.AudioHelpEventHandler("proximityHelp"));
        unitChoiceBox.setOnKeyReleased(new FxUtils.AudioHelpEventHandler("unitChoiceHelp"));
        startPointText.setOnKeyReleased(new FxUtils.AudioHelpEventHandler("startPointHelp"));
        createStartPointButton.setOnKeyReleased(new FxUtils.AudioHelpEventHandler("startPointButtonHelp"));
        currentLocationText.setOnKeyReleased(new FxUtils.AudioHelpEventHandler("currentLocationHelp"));
        stepLengthChoiceBox.setOnKeyReleased(new FxUtils.AudioHelpEventHandler("stepLengthChoiceHelp"));
        vicinityDistChoiceBox.setOnKeyReleased(new FxUtils.AudioHelpEventHandler("vicinityChoiceHelp"));
        buttonN.setOnKeyReleased(new FxUtils.AudioHelpEventHandler("buttonNHelp"));
        buttonNE.setOnKeyReleased(new FxUtils.AudioHelpEventHandler("buttonNEHelp"));
        buttonNW.setOnKeyReleased(new FxUtils.AudioHelpEventHandler("buttonNWHelp"));
        buttonS.setOnKeyReleased(new FxUtils.AudioHelpEventHandler("buttonSHelp"));
        buttonSE.setOnKeyReleased(new FxUtils.AudioHelpEventHandler("buttonSEHelp"));
        buttonSW.setOnKeyReleased(new FxUtils.AudioHelpEventHandler("buttonSWHelp"));
        buttonW.setOnKeyReleased(new FxUtils.AudioHelpEventHandler("buttonWHelp"));
        buttonE.setOnKeyReleased(new FxUtils.AudioHelpEventHandler("buttonEHelp"));
        playButton.setOnKeyReleased(new FxUtils.AudioHelpEventHandler("playButtonHelp"));
        pauseButton.setOnKeyReleased(new FxUtils.AudioHelpEventHandler("pauseButtonHelp"));
        stopButton.setOnKeyReleased(new FxUtils.AudioHelpEventHandler("stopButtonHelp"));

    }

    @FXML
    public void createProximityPoints() {
        if(!latitudeText.getInvalid() && !longitudeText.getInvalid() && !proximityText.getInvalid()) {
            LatLng poi = new LatLng(Double.parseDouble(latitudeText.getText()), Double.parseDouble(longitudeText.getText()));
            double proximityDistance = Double.parseDouble(proximityText.getText());
            refreshProximityListView(poi, proximityDistance);
        } else {
            MediaUtils.playAudioCue("error");
            setErrorsOnInvalidFields();
            if (proximityText.getInvalid()) proximityText.setEffect(invalidEffect);
            System.out.println("Point and/or vicinity is not defined.");
        }
    }

    private void refreshProximityPoints(LatLng poi) {
            double proximityDistance = vicinity;
            refreshProximityListView(poi, proximityDistance);
    }

    private void refreshProximityListView(LatLng poi, double proximityDistance) {
        namedIndividualsInProximity = OwlUtils.getIndividualsInPointProximity(poi, proximityDistance, unitType);
        Map<OWLNamedIndividual, String> points = OwlUtils.getIndividualsWithLabels(namedIndividualsInProximity);
        individualsWithLabels = FXCollections.observableMap(points);
        proximityListView.getItems().setAll(individualsWithLabels.values());
        proximityListView.getSelectionModel().selectFirst();
    }

    @FXML
    public void searchButtonHandler() {
        if (!searchText.getText().isEmpty()) {
            FxUtils.generateSearchResultsInChBox(searchResultsChoiceBox, searchText.getText());
        } else {
            MediaUtils.playAudioCue("error");
            System.out.println("Search query is empty.");
        }
    }

    @FXML
    public void makeStartPointFromGps() {
        if (!latitudeText.getInvalid() && !longitudeText.getInvalid()) {
            startPoint = new LatLng(Double.parseDouble(latitudeText.getText()), Double.parseDouble(longitudeText.getText()));
            try {
                String text = NominatimUtils.getCurrentLocationAddress(startPoint).getDisplayName();
                startPointText.setText(text);
                startPointString = text;
            } catch (IOException e) {
                System.out.println("Nominatim error: " + e.getMessage() + "cause: " + e.getCause());
                e.printStackTrace();
            }
        } else {
            MediaUtils.playAudioCue("error");
            setErrorsOnInvalidFields();
            System.out.println("Invalid gps coords.");
        }
    }

    private void refreshCurrentLocation() throws IOException {
        if (startPoint != null) {
            Address address = NominatimUtils.getCurrentLocationAddress(startPoint);
            currentLocationText.setText(address.getDisplayName());
            currentLocationString = address.getDisplayName();
        } else {
            makeStartPointFromGps();
        }
    }

    private void setErrorsOnInvalidFields() {
        if (latitudeText.getInvalid()) latitudeText.setEffect(invalidEffect);
        if (longitudeText.getInvalid()) longitudeText.setEffect(invalidEffect);
    }

    @FXML
    public void playAudio() {
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
            player.dispose();
        }
    }

    @FXML
    public void pauseAudio() {
        MediaUtils.pause(player);
    }

    @FXML
    public void stopAudio() {
        MediaUtils.stop(player);
        audioTracks.clear();
    }

    private class NumPadTraversalHandler implements EventHandler<KeyEvent> {

        @Override
        public void handle(KeyEvent event) {
            KeyCode code = event.getCode();
            if (code == KeyCode.NUMPAD8) {
                event.consume();
                buttonN.requestFocus();
                TravelButtonsHandler tbh = new TravelButtonsHandler(LatLngTool.Bearing.NORTH);
                tbh.handle(new ActionEvent());
            }
            if (code == KeyCode.NUMPAD2) {
                event.consume();
                buttonS.requestFocus();
                TravelButtonsHandler tbh = new TravelButtonsHandler(LatLngTool.Bearing.SOUTH);
                tbh.handle(new ActionEvent());
            }
            if (code == KeyCode.NUMPAD4) {
                event.consume();
                buttonW.requestFocus();
                TravelButtonsHandler tbh = new TravelButtonsHandler(LatLngTool.Bearing.WEST);
                tbh.handle(new ActionEvent());
            }
            if (code == KeyCode.NUMPAD6) {
                event.consume();
                buttonE.requestFocus();
                TravelButtonsHandler tbh = new TravelButtonsHandler(LatLngTool.Bearing.EAST);
                tbh.handle(new ActionEvent());
            }
            if (code == KeyCode.NUMPAD7) {
                event.consume();
                buttonNW.requestFocus();
                TravelButtonsHandler tbh = new TravelButtonsHandler(LatLngTool.Bearing.NORTH_WEST);
                tbh.handle(new ActionEvent());
            }
            if (code == KeyCode.NUMPAD9) {
                event.consume();
                buttonNE.requestFocus();
                TravelButtonsHandler tbh = new TravelButtonsHandler(LatLngTool.Bearing.NORTH_EAST);
                tbh.handle(new ActionEvent());
            }
            if (code == KeyCode.NUMPAD1) {
                event.consume();
                buttonSW.requestFocus();
                TravelButtonsHandler tbh = new TravelButtonsHandler(LatLngTool.Bearing.SOUTH_WEST);
                tbh.handle(new ActionEvent());
            }
            if (code == KeyCode.NUMPAD3) {
                event.consume();
                buttonSE.requestFocus();
                TravelButtonsHandler tbh = new TravelButtonsHandler(LatLngTool.Bearing.SOUTH_EAST);
                tbh.handle(new ActionEvent());
            }
        }
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
                LatLng nextPoint = LatLngTool.travel(startPoint, this.bearing, stepLength, unitType);
                refreshProximityPoints(nextPoint);
                try {
                    refreshCurrentLocation();
                } catch (IOException e) {
                    System.out.println("Nominatim error: " + e.getMessage() + "cause: " + e.getCause());
                    e.printStackTrace();
                }
                startPoint = nextPoint;
                System.out.println("NEXT POINT: " + nextPoint.toString());
            } else if (!latitudeText.getInvalid() && !longitudeText.getInvalid()){
                makeStartPointFromGps();
                this.handle(event);
            } else {
                MediaUtils.playAudioCue("error");
                setErrorsOnInvalidFields();
                System.out.println("Empty GPS coordinates.");
            }
        }
    }

    private static class VoiceCueEventHandler implements EventHandler<KeyEvent> {

        @Override
        public void handle(KeyEvent event) {
            KeyCode code = event.getCode();
            if(code == KeyCode.NUMPAD5) {
                if (!currentLocationString.isEmpty())
                    new Thread (() -> PollyUtils.play(currentLocationString)).start();
                else System.out.println("No current location.");
            }
        }
    }
}
