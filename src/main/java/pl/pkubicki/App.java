package pl.pkubicki;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * author: Przemysław Kubicki
 */
public class App extends Application {

    private static Scene scene;
    private static Stage stage;

    @Override
    public void start(Stage stage) throws IOException {
        App.stage = stage;
        scene = new Scene(loadFXML("fxml/Primary"), 340, 440);
        App.stage.setTitle("PathPlayer 1.0");
        App.stage.setScene(scene);
        App.stage.show();
        App.stage.addEventHandler(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
            if (KeyCode.ESCAPE == event.getCode()) {
                try {
                    App.stage.setWidth(356);
                    App.stage.setHeight(479);
                    App.setRoot("fxml/Primary");
                    App.centerOnStage();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (KeyCode.F12 == event.getCode()) {
                App.stage.setFullScreen(true);
            }
        });
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    public static void setRoot(String fxml, double width, double height) throws IOException {
        stage.setWidth(width);
        stage.setHeight(height);
        setRoot(fxml);
    }

    public static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void centerOnStage() {
        App.stage.centerOnScreen();
    }
    public static void main(String[] args) {
        launch();
    }
}