package pl.pkubicki.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import pl.pkubicki.App;
import pl.pkubicki.util.FxUtils;
import pl.pkubicki.util.MediaUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PrimaryController implements Initializable {

    @FXML private Button loginButton;
    @FXML private Button owlPanelButton;
    @FXML private Button freeTravelButton;
    @FXML private Button routeTravelButton;

    @FXML
    private void switchToSecondary() throws IOException {
        MediaUtils.playAudioCue("push");
        App.setRoot("fxml/Secondary");
        App.centerOnStage();
    }

    @FXML
    private void switchToRouteTravel() throws IOException {
        MediaUtils.playAudioCue("push");
        App.setRoot("fxml/RouteTravel", 700, 800);
        App.centerOnStage();
    }

    @FXML
    private void switchToFreeTravel() throws IOException {
        MediaUtils.playAudioCue("push");
        App.setRoot("fxml/FreeTravel", 900, 900);
        App.centerOnStage();
    }

    @FXML
    private void switchToOwlPanel() throws IOException {
        MediaUtils.playAudioCue("push");
        App.setRoot("fxml/OwlPanel", 1000, 600);
        App.centerOnStage();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        FxUtils.getFocusListener(loginButton, true);
        FxUtils.getFocusListener(owlPanelButton, true);
        FxUtils.getFocusListener(freeTravelButton, true);
        FxUtils.getFocusListener(routeTravelButton, true);
    }

    public void close() {
        Stage stage = (Stage) owlPanelButton.getScene().getWindow();
        stage.close();
    }
}
