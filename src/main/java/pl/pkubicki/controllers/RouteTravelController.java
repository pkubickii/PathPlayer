package pl.pkubicki.controllers;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.util.LengthUnit;
import fr.dudie.nominatim.model.Address;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import pl.pkubicki.extensions.ValidatedTextField;
import pl.pkubicki.util.FxUtils;
import pl.pkubicki.util.HopperUtils;
import pl.pkubicki.util.MediaUtils;
import pl.pkubicki.util.OwlUtils;

import java.net.URL;
import java.util.*;

public class RouteTravelController implements Initializable {

    @FXML private Label labelForSearchStartText;
    @FXML private TextField searchStartText;
    @FXML private Label labelForSearchStartResults;
    @FXML private ChoiceBox<Address> searchStartResultsChBox;
    @FXML private Label labelForSearchEndText;
    @FXML private TextField searchEndText;
    @FXML private Label labelForSearchEndResults;
    @FXML private ChoiceBox<Address> searchEndResultsChBox;
    @FXML private Label labelForStartLatitude;
    @FXML private ValidatedTextField startLatitudeText;
    @FXML private Label labelForStartLongitude;
    @FXML private ValidatedTextField startLongitudeText;
    @FXML private Label labelForEndLatitude;
    @FXML private ValidatedTextField endLatitudeText;
    @FXML private Label labelForEndLongitude;
    @FXML private ValidatedTextField endLongitudeText;
    @FXML private Label labelForVicinityChBox;
    @FXML private ChoiceBox<Double> vicinityChBox;
    @FXML private ListView<String> routePointsListView;

    @FXML private Button searchStartButton;
    @FXML private Button searchEndButton;
    @FXML private Button generateRouteButton;
    @FXML private Button playButton;
    @FXML private Button pauseButton;
    @FXML private Button stopButton;

    private static Set<OWLNamedIndividual> individualsOnRoute = new HashSet<>();
    private static LinkedList<Media> audioTracks = new LinkedList<>();
    private static List<LatLng> route = new ArrayList<>();
    private static ObservableList<Double> vicinityDistances = FXCollections.emptyObservableList();
    private static double vicinity = 50.0;
    private static final LengthUnit UNIT = LengthUnit.METER;
    private static MediaPlayer player = null;
    private static Media media = null;
    private static final Effect invalidEffect = new DropShadow(BlurType.GAUSSIAN, Color.RED, 9, 0.9, 2, 2);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeSearchResultsListeners();
        initializeVicinityDistances();
        searchStartText.setText("dworzec pkp siedlce");
        searchEndText.setText("3 maja 54 siedlce");
        initializeVicinityDistancesListenerToRefreshRoutePoints();
        searchStartText.setOnKeyReleased(new FxUtils.SubmitTextFieldHandler(searchStartResultsChBox, searchStartText));
        searchEndText.setOnKeyReleased(new FxUtils.SubmitTextFieldHandler(searchEndResultsChBox, searchEndText));
        initializeFocusListener(searchStartText, labelForSearchStartText, true);
        initializeFocusListener(searchStartResultsChBox, labelForSearchStartResults, false);
        initializeFocusListener(searchEndText, labelForSearchEndText, true);
        initializeFocusListener(searchEndResultsChBox, labelForSearchEndResults, false);
        initializeFocusListener(startLatitudeText, labelForStartLatitude, true);
        initializeFocusListener(startLongitudeText, labelForStartLongitude, true);
        initializeFocusListener(endLatitudeText, labelForEndLatitude, true);
        initializeFocusListener(endLongitudeText, labelForEndLongitude, true);
        initializeFocusListener(vicinityChBox, labelForVicinityChBox, false);
        initializeFocusListener(vicinityChBox, false);

        initializeFocusListener(searchStartButton, true);
        initializeFocusListener(searchEndButton, true);
        initializeFocusListener(generateRouteButton, true);
        initializeFocusListener(playButton, true);
        initializeFocusListener(pauseButton, true);
        initializeFocusListener(stopButton, true);
    }
    private void initializeSearchResultsListeners() {
        FxUtils.updateGpsValuesFromSearchResultsChoiceBox(searchStartResultsChBox, startLatitudeText, startLongitudeText);
        FxUtils.getChoiceBoxListenersForSound(searchStartResultsChBox);
        FxUtils.updateGpsValuesFromSearchResultsChoiceBox(searchEndResultsChBox, endLatitudeText, endLongitudeText);
        FxUtils.getChoiceBoxListenersForSound(searchEndResultsChBox);
    }

    private void initializeVicinityDistances() {
        vicinityDistances = FxUtils.getObListForVicinity();
        vicinityChBox.getItems().clear();
        vicinityChBox.setItems(vicinityDistances);
        vicinityChBox.setValue(50.0);
        FxUtils.getChoiceBoxListenersForSound(vicinityChBox);
    }

    private void initializeVicinityDistancesListenerToRefreshRoutePoints() {
        vicinityChBox.valueProperty().addListener( (obs, oldVal, newVal) -> {
            if (newVal != null) {
                vicinity = Double.parseDouble(newVal.toString());
                refreshPointsOnRoute();
            }
        });
    }

    private void initializeFocusListener(Node focusedNode, Node nodeLabel, boolean sound) {
        FxUtils.getFocusListener(focusedNode, nodeLabel, sound);
    }

    private void initializeFocusListener(Node focusedNode, boolean sound) {
        FxUtils.getFocusListener(focusedNode, sound);
    }

    @FXML
    public void searchStartButtonHandler() {
        if (!searchStartText.getText().isEmpty()) {
            FxUtils.generateSearchResultsInChBox(searchStartResultsChBox, searchStartText.getText());
            searchStartResultsChBox.requestFocus();
        } else {
            System.out.println("Search query is empty.");
        }
    }
    public void searchEndButtonHandler() {
        if (!searchEndText.getText().isEmpty()) {
            FxUtils.generateSearchResultsInChBox(searchEndResultsChBox, searchEndText.getText());
            searchEndResultsChBox.requestFocus();
        } else {
            System.out.println("Search query is empty.");
        }
    }

    public void generateRouteButtonHandler() {
        if (isFormValid()) {
            refreshPointsOnRoute();
        } else {
            MediaUtils.playAudioCue("error");
            setErrorsOnInvalidFields();
        }
    }

    private void refreshPointsOnRoute() {
        if (!route.isEmpty()) {
            routePointsListView.getItems().setAll(getObIndividualsOnRoute().values());
            routePointsListView.getSelectionModel().selectFirst();
        } else if (isFormValid()){
            setRoute();
            refreshPointsOnRoute();
        }
    }

    private void setRoute() {
        if(isFormValid()) {
            route = HopperUtils.getRoute(
                    Double.parseDouble(startLatitudeText.getText()),
                    Double.parseDouble(startLongitudeText.getText()),
                    Double.parseDouble(endLatitudeText.getText()),
                    Double.parseDouble(endLongitudeText.getText())
            );
        } else {
            MediaUtils.playAudioCue("error");
            setErrorsOnInvalidFields();
            System.out.println("Missing gps coordinates.");
        }
    }

    private boolean isFormValid() {
        return !(startLatitudeText.getInvalid() ||
                startLongitudeText.getInvalid() ||
                endLatitudeText.getInvalid() ||
                endLongitudeText.getInvalid());
    }

    private void setErrorsOnInvalidFields() {
        if (startLatitudeText.getInvalid()) startLatitudeText.setEffect(invalidEffect);
        if (startLongitudeText.getInvalid()) startLongitudeText.setEffect(invalidEffect);
        if (endLatitudeText.getInvalid()) endLatitudeText.setEffect(invalidEffect);
        if (endLongitudeText.getInvalid()) endLongitudeText.setEffect(invalidEffect);
    }

    private void refreshAudioTracks() {
        if (!individualsOnRoute.isEmpty()) {
            audioTracks = FxUtils.getAudioTracks(individualsOnRoute);
        } else {
            audioTracks.clear();
            System.out.println("Nothing to play.");
        }
    }

    private ObservableMap<OWLNamedIndividual, String> getObIndividualsOnRoute() {
        individualsOnRoute = getOwlNamedIndividualsOnRoute();
        Map<OWLNamedIndividual, String> individualsOnRouteWithLabels = OwlUtils.getIndividualsWithLabels(individualsOnRoute);
        return FXCollections.observableMap(individualsOnRouteWithLabels);
    }

    private Set<OWLNamedIndividual> getOwlNamedIndividualsOnRoute() {
        if (!route.isEmpty()) {
            return OwlUtils.getIndividualsInRouteProximity(route, vicinity, UNIT);
        } else
            return new HashSet<>();
    }

    public void playAudio() {
        refreshAudioTracks();
        if (player != null && player.getStatus() == MediaPlayer.Status.PLAYING) return;
        if(!route.isEmpty()) {
            routePointsListView.getSelectionModel().selectFirst();
            LinkedList<Media> tempAudioTracks = new LinkedList<>();
            Collections.addAll(tempAudioTracks, audioTracks.toArray(new Media[0]));
            playAudioTracks(tempAudioTracks);
        } else {
            System.out.println("No route to play with.");
        }
    }

    private void playAudioTracks(LinkedList<Media> audioList) {
        if (!audioList.isEmpty()) {
            media = audioList.poll();
            player = new MediaPlayer(media);
            player.setOnEndOfMedia(() -> {
                routePointsListView.getSelectionModel().selectNext();
                playAudioTracks(audioList);
            });
            player.play();
        } else {
            System.out.println("End of audio route.");
            player.dispose();
        }
    }

    public void pauseAudio() {
        MediaUtils.pause(player);
    }

    public void stopAudio() {
        MediaUtils.stop(player);
    }


}
