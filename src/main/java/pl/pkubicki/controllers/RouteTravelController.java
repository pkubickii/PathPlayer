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
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import pl.pkubicki.util.FxUtils;
import pl.pkubicki.util.HopperUtils;
import pl.pkubicki.util.OwlUtils;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class RouteTravelController implements Initializable {
    @FXML private ChoiceBox vicinityChBox;
    @FXML private ListView routePointsList;
    @FXML private TextField startLatitudeText;
    @FXML private TextField startLongitudeText;
    @FXML private TextField endLatitudeText;
    @FXML private TextField endLongitudeText;
    @FXML private ChoiceBox<Address> searchStartResultsChBox;
    @FXML private ChoiceBox<Address> searchEndResultsChBox;
    @FXML private TextField searchStartText;
    @FXML private TextField searchEndText;

    private static List<LatLng> route;
    private static ObservableList vicinityDistances = FXCollections.emptyObservableList();
    private static double vicinity = 50.0;
    private static final LengthUnit UNIT = LengthUnit.METER;

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
                refreshRoute();
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
        getRoute();
        refreshRoute();
    }

    private void refreshRoute() {
        if (route != null) {
            routePointsList.getItems().setAll(getObIndividualsInRouteProximity().values());
            routePointsList.getSelectionModel().selectFirst();
        } else {
            System.out.println("Generate ROUTE first.");
        }
    }

    private ObservableMap<OWLNamedIndividual, String> getObIndividualsInRouteProximity() {
        Set<OWLNamedIndividual> individualsInRouteProximity = OwlUtils.getIndividualsInRouteProximity(route, vicinity, UNIT);
        Map<OWLNamedIndividual, String> individualsWithLabels = OwlUtils.getIndividualsWithLabels(individualsInRouteProximity);
        ObservableMap<OWLNamedIndividual, String> obIndividualsInRouteProximity = FXCollections.observableMap(individualsWithLabels);
        return obIndividualsInRouteProximity;
    }


    private void getRoute() {
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


    public void playAudio(ActionEvent actionEvent) {
    }

    public void pauseAudio(ActionEvent actionEvent) {
    }

    public void stopAudio(ActionEvent actionEvent) {
    }
}
