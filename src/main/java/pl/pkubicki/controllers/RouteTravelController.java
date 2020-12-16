package pl.pkubicki.controllers;

import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import fr.dudie.nominatim.model.Address;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import pl.pkubicki.util.FxUtils;
import pl.pkubicki.util.HopperUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class RouteTravelController implements Initializable {
    @FXML private ListView routePointsList;
    @FXML private TextField startLatitudeText;
    @FXML private TextField startLongitudeText;
    @FXML private TextField endLatitudeText;
    @FXML private TextField endLongitudeText;
    @FXML private ChoiceBox<Address> searchStartResultsChBox;
    @FXML private ChoiceBox<Address> searchEndResultsChBox;
    @FXML private TextField searchStartText;
    @FXML private TextField searchEndText;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FxUtils.updateGpsValuesFromSearchChoice(searchStartResultsChBox, startLatitudeText, startLongitudeText);
        FxUtils.updateGpsValuesFromSearchChoice(searchEndResultsChBox, endLatitudeText, endLongitudeText);
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
        PointList pointList = HopperUtils.getRoute(
                Double.parseDouble(startLatitudeText.getText()),
                Double.parseDouble(startLongitudeText.getText()),
                Double.parseDouble(endLatitudeText.getText()),
                Double.parseDouble(endLongitudeText.getText())
                );

        List<GHPoint> list = new ArrayList<>();
        pointList.forEach(p -> {
            list.add(p);
        });
        ObservableList<GHPoint> obPoints = FXCollections.observableList(list);
        routePointsList.setItems(obPoints);
    }



}
