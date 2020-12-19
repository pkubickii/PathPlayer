package pl.pkubicki.controllers;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.util.LengthUnit;
import fr.dudie.nominatim.model.Address;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayerBuilder;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import pl.pkubicki.util.FxUtils;
import pl.pkubicki.util.HopperUtils;
import pl.pkubicki.util.MediaUtils;
import pl.pkubicki.util.OwlUtils;
import sun.awt.image.ImageWatched;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class RouteTravelController implements Initializable {
    @FXML private ChoiceBox vicinityChBox;
    @FXML private ListView routePointsListView;
    @FXML private TextField startLatitudeText;
    @FXML private TextField startLongitudeText;
    @FXML private TextField endLatitudeText;
    @FXML private TextField endLongitudeText;
    @FXML private ChoiceBox<Address> searchStartResultsChBox;
    @FXML private ChoiceBox<Address> searchEndResultsChBox;
    @FXML private TextField searchStartText;
    @FXML private TextField searchEndText;

    private static Set<OWLNamedIndividual> individualsOnRoute = new HashSet<>();
    private static LinkedList<Media> audioTracks = new LinkedList<>();
    private static List<LatLng> route = new ArrayList<>();
    private static ObservableList vicinityDistances = FXCollections.emptyObservableList();
    private static double vicinity = 50.0;
    private static final LengthUnit UNIT = LengthUnit.METER;
    private static MediaPlayer player = null;
    private static Media media = null;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FxUtils.updateGpsValuesFromSearchChoice(searchStartResultsChBox, startLatitudeText, startLongitudeText);
        FxUtils.updateGpsValuesFromSearchChoice(searchEndResultsChBox, endLatitudeText, endLongitudeText);
        FxUtils.initializeVicinityDistances(vicinityChBox, vicinityDistances);
        searchStartText.setText("dworzec pkp siedlce");
        searchEndText.setText("3 maja 54 siedlce");
        initializeVicinityDistancesListenerToRefreshRoutePoints();
    }
    private void initializeVicinityDistancesListenerToRefreshRoutePoints() {
        vicinityChBox.valueProperty().addListener( (obs, oldVal, newVal) -> {
            if (newVal != null) {
                vicinity = Double.parseDouble(newVal.toString());
                refreshPointsOnRoute();
            }
        });
    }
    @FXML
    public void searchStartButtonHandler(ActionEvent actionEvent) throws IOException {
        if (!searchStartText.getText().isEmpty()) {
            FxUtils.generateSearchResultsInChBox(searchStartResultsChBox, searchStartText.getText());
        } else {
            System.out.println("Search query is empty.");
        }
    }
    public void searchEndButtonHandler(ActionEvent actionEvent) throws IOException {
        if (!searchEndText.getText().isEmpty()) {
            FxUtils.generateSearchResultsInChBox(searchEndResultsChBox, searchEndText.getText());
        } else {
            System.out.println("Search query is empty.");
        }
    }

    public void generateRouteButtonHandler(ActionEvent actionEvent) {
        setRoute();
        refreshPointsOnRoute();
        refreshAudioTracks();
    }

    private void refreshPointsOnRoute() {
        if (!route.isEmpty()) {
            routePointsListView.getItems().setAll(getObIndividualsOnRoute().values());
            routePointsListView.getSelectionModel().selectFirst();
        } else {
            System.out.println("Generate ROUTE first.");
        }
    }

    private ObservableMap<OWLNamedIndividual, String> getObIndividualsOnRoute() {
        individualsOnRoute = getOwlNamedIndividualsOnRoute();
        Map<OWLNamedIndividual, String> individualsOnRouteWithLabels = OwlUtils.getIndividualsWithLabels(individualsOnRoute);
        ObservableMap<OWLNamedIndividual, String> obIndOnRouteWithLabels = FXCollections.observableMap(individualsOnRouteWithLabels);
        return obIndOnRouteWithLabels;
    }

    private Set<OWLNamedIndividual> getOwlNamedIndividualsOnRoute() {
        if (!route.isEmpty()) {
            return OwlUtils.getIndividualsInRouteProximity(route, vicinity, UNIT);
        } else
            return new HashSet<>();
    }


    private void setRoute() {
        if(!(startLatitudeText.getText().isEmpty() || startLongitudeText.getText().isEmpty() || endLatitudeText.getText().isEmpty() || endLongitudeText.getText().isEmpty())) {
            route = HopperUtils.getRoute(
                    Double.parseDouble(startLatitudeText.getText()),
                    Double.parseDouble(startLongitudeText.getText()),
                    Double.parseDouble(endLatitudeText.getText()),
                    Double.parseDouble(endLongitudeText.getText())
            );
        } else {
            System.out.println("Missing gps coordinates.");
        }
    }
    private void refreshAudioTracks() {
        if (!individualsOnRoute.isEmpty()) {
            audioTracks = FxUtils.getAudioTracks(individualsOnRoute);
        } else {
            System.out.println("List with points is empty.");
        }
    }

    public void playAudio(ActionEvent actionEvent) {
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
            System.out.println("No audio to play");
        }
    }

    public void pauseAudio(ActionEvent actionEvent) {
        MediaUtils.pause(player);
    }

    public void stopAudio(ActionEvent actionEvent) {
        MediaUtils.stop(player);
    }
}
