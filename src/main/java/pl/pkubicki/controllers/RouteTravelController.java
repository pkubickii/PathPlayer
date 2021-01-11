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
import pl.pkubicki.util.*;

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
    @FXML private Label labelForLatitudeStart;
    @FXML private ValidatedTextField latitudeStartText;
    @FXML private Label labelForLongitudeStart;
    @FXML private ValidatedTextField longitudeStartText;
    @FXML private Label labelForLatitudeEnd;
    @FXML private ValidatedTextField latitudeEndText;
    @FXML private Label labelForLongitudeEnd;
    @FXML private ValidatedTextField longitudeEndText;
    @FXML private Label labelForVicinityChBox;
    @FXML private ChoiceBox<Double> vicinityDistChoiceBox;
    @FXML private ListView<String> routePointsListView;

    @FXML private Button searchStartButton;
    @FXML private Button searchEndButton;
    @FXML private Button generateRouteButton;
    @FXML private Button playButton;
    @FXML private Button pauseButton;
    @FXML private Button stopButton;

    private static LinkedHashSet<OWLNamedIndividual> individualsOnRoute = new LinkedHashSet<>();
    private static LinkedList<Media> audioTracks = new LinkedList<>();
    private static List<LatLng> route = new ArrayList<>();
    private static ObservableList<Double> vicinityDistances = FXCollections.emptyObservableList();
    private static double vicinity = 50.0;
    private static final LengthUnit UNIT = LengthUnit.METER;
    private static MediaPlayer player = null;
    private static Media media = null;
    private static final Effect invalidEffect = new DropShadow(BlurType.GAUSSIAN, Color.RED, 9, 0.9, 2, 2);

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
        if (isFormValid()) {
            setRoute();
            routePointsListView.getItems().setAll(getObIndividualsOnRoute().values());
            routePointsListView.getSelectionModel().selectFirst();
        }
    }

    private void setRoute() {
        if(isFormValid() &&
                isPointInRange(latitudeStartText.getText(), longitudeStartText.getText()) &&
                        isPointInRange(latitudeEndText.getText(), longitudeEndText.getText())) {
            route = HopperUtils.getRoute(
                    Double.parseDouble(latitudeStartText.getText()),
                    Double.parseDouble(longitudeStartText.getText()),
                    Double.parseDouble(latitudeEndText.getText()),
                    Double.parseDouble(longitudeEndText.getText())
            );
        } else {
            MediaUtils.playAudioCue("error");
            setErrorsOnInvalidFields();
            if (!isFormValid())
                System.out.println("Missing gps coordinates.");
            else
                System.out.println("Gps point out of range. latitude: [19.16651861741808, 23.16475711386108] longitude: [50.97398685605795, 53.55018627383231]");
        }
    }

    private boolean isFormValid() {
        return !(latitudeStartText.getInvalid() ||
                longitudeStartText.getInvalid() ||
                latitudeEndText.getInvalid() ||
                longitudeEndText.getInvalid());
    }

    private boolean isPointInRange(String latitude, String longitude) {
        double lat = Double.parseDouble(latitude);
        double lng = Double.parseDouble(longitude);
        return isRangeValid(lat, lng);
    }

    private boolean isRangeValid(double latitude, double longitude) {
        // values for mazowieckie.osm.pbf
        double minLatitude = 19.16651861741808;
        double maxLatitude = 23.16475711386108;
        double minLongitude = 50.97398685605795;
        double maxLongitude = 53.55018627383231;
        return minLatitude < latitude &&
                latitude < maxLatitude &&
                minLongitude < longitude &&
                longitude < maxLongitude;
    }

    private void setErrorsOnInvalidFields() {
        if (latitudeStartText.getInvalid()) latitudeStartText.setEffect(invalidEffect);
        if (longitudeStartText.getInvalid()) longitudeStartText.setEffect(invalidEffect);
        if (latitudeEndText.getInvalid()) latitudeEndText.setEffect(invalidEffect);
        if (longitudeEndText.getInvalid()) longitudeEndText.setEffect(invalidEffect);
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
        LinkedHashMap<OWLNamedIndividual, String> individualsOnRouteWithLabels = OwlUtils.getLinkedIndividualsWithLabels(individualsOnRoute);
        return FXCollections.observableMap(individualsOnRouteWithLabels);
    }

    private LinkedHashSet<OWLNamedIndividual> getOwlNamedIndividualsOnRoute() {
        if (!route.isEmpty()) {
            return OwlUtils.getOwlPointsCloseToRoute(route, vicinity, UNIT);
        } else
            return new LinkedHashSet<>();
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeSearchResultsListeners();
        initializeVicinityDistances();
        searchStartText.setText("dworzec pkp siedlce");
        searchEndText.setText("3 maja 54 siedlce");
        initializeVicinityDistancesListenerToRefreshRoutePoints();
        searchStartText.setOnKeyPressed(new FxUtils.SubmitTextFieldHandler(searchStartResultsChBox, searchStartText));
        searchEndText.setOnKeyPressed(new FxUtils.SubmitTextFieldHandler(searchEndResultsChBox, searchEndText));
        initializeFocusListeners();
        initializeAudioHelpers();
    }
    private void initializeSearchResultsListeners() {
        FxUtils.updateGpsValuesFromSearchResultsChoiceBox(searchStartResultsChBox, latitudeStartText, longitudeStartText);
        FxUtils.getChoiceBoxListenersForSound(searchStartResultsChBox);
        FxUtils.updateGpsValuesFromSearchResultsChoiceBox(searchEndResultsChBox, latitudeEndText, longitudeEndText);
        FxUtils.getChoiceBoxListenersForSound(searchEndResultsChBox);
    }

    private void initializeVicinityDistances() {
        vicinityDistances = FxUtils.getObListForVicinity();
        vicinityDistChoiceBox.getItems().clear();
        vicinityDistChoiceBox.setItems(vicinityDistances);
        vicinityDistChoiceBox.setValue(50.0);
        FxUtils.getChoiceBoxListenersForSound(vicinityDistChoiceBox);
    }

    private void initializeVicinityDistancesListenerToRefreshRoutePoints() {
        vicinityDistChoiceBox.valueProperty().addListener( (obs, oldVal, newVal) -> {
            if (newVal != null) {
                vicinity = Double.parseDouble(newVal.toString());
                refreshPointsOnRoute();
            }
        });
    }

    private void initializeFocusListeners() {
        initializeFocusListener(searchStartText, labelForSearchStartText, true);
        initializeFocusListener(searchStartResultsChBox, labelForSearchStartResults, false);
        initializeFocusListener(searchEndText, labelForSearchEndText, true);
        initializeFocusListener(searchEndResultsChBox, labelForSearchEndResults, false);
        initializeFocusListener(latitudeStartText, labelForLatitudeStart, true);
        initializeFocusListener(longitudeStartText, labelForLongitudeStart, true);
        initializeFocusListener(latitudeEndText, labelForLatitudeEnd, true);
        initializeFocusListener(longitudeEndText, labelForLongitudeEnd, true);
        initializeFocusListener(vicinityDistChoiceBox, labelForVicinityChBox, false);
        initializeFocusListener(vicinityDistChoiceBox, false);

        initializeFocusListener(searchStartButton, true);
        initializeFocusListener(searchEndButton, true);
        initializeFocusListener(generateRouteButton, true);
        initializeFocusListener(playButton, true);
        initializeFocusListener(pauseButton, true);
        initializeFocusListener(stopButton, true);
    }

    private void initializeFocusListener(Node focusedNode, Node nodeLabel, boolean sound) {
        FxUtils.getFocusListener(focusedNode, nodeLabel, sound);
    }

    private void initializeFocusListener(Node focusedNode, boolean sound) {
        FxUtils.getFocusListener(focusedNode, sound);
    }

    private void initializeAudioHelpers() {
        searchStartText.setOnKeyReleased(new FxUtils.AudioHelpEventHandler("searchStartHelp"));
        searchStartButton.setOnKeyReleased(new FxUtils.AudioHelpEventHandler("searchStartButtonHelp"));
        searchStartResultsChBox.setOnKeyReleased(new FxUtils.AudioHelpEventHandler("searchStartResultsHelp"));
        latitudeStartText.setOnKeyReleased(new FxUtils.AudioHelpEventHandler("latitudeStartHelp"));
        longitudeStartText.setOnKeyReleased(new FxUtils.AudioHelpEventHandler("longitudeStartHelp"));

        searchEndText.setOnKeyReleased(new FxUtils.AudioHelpEventHandler("searchEndHelp"));
        searchEndButton.setOnKeyReleased(new FxUtils.AudioHelpEventHandler("searchEndButtonHelp"));
        searchEndResultsChBox.setOnKeyReleased(new FxUtils.AudioHelpEventHandler("searchEndResultsHelp"));
        latitudeEndText.setOnKeyReleased(new FxUtils.AudioHelpEventHandler("latitudeEndHelp"));
        longitudeEndText.setOnKeyReleased(new FxUtils.AudioHelpEventHandler("longitudeEndHelp"));

        generateRouteButton.setOnKeyReleased(new FxUtils.AudioHelpEventHandler("generateRouteButtonHelp"));
        vicinityDistChoiceBox.setOnKeyReleased(new FxUtils.AudioHelpEventHandler("vicinityRouteHelp"));

        playButton.setOnKeyReleased(new FxUtils.AudioHelpEventHandler("playButtonHelp"));
        pauseButton.setOnKeyReleased(new FxUtils.AudioHelpEventHandler("pauseButtonHelp"));
        stopButton.setOnKeyReleased(new FxUtils.AudioHelpEventHandler("stopButtonHelp"));

        routePointsListView.setOnKeyReleased(new FxUtils.AudioHelpEventHandler("proximityPointsHelp"));

        initializeSearchResultsAudioRead();
        initializeGpsAudioRead();
        initializeVicinityAudioRead();
    }

    private void initializeSearchResultsAudioRead() {
        AudioUtils.initializeChoiceBoxAudioRead(searchStartResultsChBox, "Brak wyników wyszukiwania. Wprowadź zapytanie w wyszukiwarkę i wciśnij ENTER.");
        AudioUtils.initializeChoiceBoxAudioRead(searchEndResultsChBox, "Brak wyników wyszukiwania. Wprowadź zapytanie w wyszukiwarkę i wciśnij ENTER.");
    }

    private void initializeGpsAudioRead() {
        AudioUtils.initializeTextAudioRead(latitudeStartText, "Pole startowe szerokości geograficznej jest puste.");
        AudioUtils.initializeTextAudioRead(longitudeStartText, "Pole startowe długości geograficznej jest puste.");
        AudioUtils.initializeTextAudioRead(latitudeEndText, "Pole startowe szerokości geograficznej jest puste.");
        AudioUtils.initializeTextAudioRead(longitudeEndText, "Pole startowe długości geograficznej jest puste.");
    }

    private void initializeVicinityAudioRead() {
        AudioUtils.initializeChoiceBoxAudioRead(vicinityDistChoiceBox, "Brak wartości.");
    }

}
