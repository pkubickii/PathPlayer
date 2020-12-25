package pl.pkubicki.controllers;

import javafx.fxml.FXML;
import pl.pkubicki.App;

import java.io.IOException;

public class PrimaryController {

    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("fxml/Secondary");
    }

    @FXML
    private void switchToRouteTravel() throws IOException {
        App.setRoot("fxml/RouteTravel", 700, 800);
    }

    @FXML
    private void switchToFreeTravel() throws IOException {
        App.setRoot("fxml/FreeTravel", 800, 800);
    }

    @FXML
    private void switchToOwlPanel() throws IOException {
        App.setRoot("fxml/OwlPanel", 800, 600);
    }
}
