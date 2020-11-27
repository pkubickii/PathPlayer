package pl.pkubicki.controllers;

import java.io.IOException;
import javafx.fxml.FXML;
import pl.pkubicki.App;

public class SecondaryController {

    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("fxml/primary");
    }
}