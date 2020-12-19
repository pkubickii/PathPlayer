package pl.pkubicki.util;

import fr.dudie.nominatim.model.Address;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.Media;
import javafx.util.StringConverter;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import java.io.IOException;
import java.util.*;

public class FxUtils {
    public static void generateSearchResultsInChBox(ChoiceBox<Address> choiceBox, String query){
        ObservableList<Address> observableList = null;
        try {
            observableList = NominatimUtils.search(query);
        } catch (IOException e) {
            e.printStackTrace();
        }
        choiceBox.getItems().clear();
        choiceBox.setItems(observableList);
        setSearchResultsChBoxStringConverter(choiceBox);
        choiceBox.getSelectionModel().selectFirst();
    }
    private static void setSearchResultsChBoxStringConverter(ChoiceBox<Address> choiceBox) {
        choiceBox.setConverter(new StringConverter<Address>() {
            @Override
            public String toString(Address object) {
                return object.getDisplayName();
            }

            @Override
            public Address fromString(String string) {
                return choiceBox.getItems().stream().filter(address ->
                        address.getDisplayName().equals(string)).findFirst().orElse(null);
            }
        });
    }

    public static void updateGpsValuesFromSearchChoice(ChoiceBox<Address> cB, TextField lat, TextField lng) {
        cB.valueProperty().addListener((obs, oldVal, newVal) -> {
            if(newVal != null) {
                lat.setText(String.valueOf(newVal.getLatitude()));
                lng.setText(String.valueOf(newVal.getLongitude()));
            }
        });
    }

    public static ObservableList<Double> getObListForVicinity() {
        List<Double> vicinityDistances = new ArrayList<Double>() {
            {
                add(10.0);
                add(20.0);
                add(30.0);
                add(40.0);
                add(50.0);
                add(100.0);
                add(200.0);
            }
        };
        return FXCollections.observableList(vicinityDistances);
    }
    public static LinkedList<Media> getAudioTracks(Set<OWLNamedIndividual> proximityPoints) {
        LinkedList<Media> audioTracks = new LinkedList<>();
        Map<OWLNamedIndividual, String> audioFileNames = OwlUtils.getAudioFileNames(proximityPoints);
        audioFileNames.forEach( (k , v) -> audioTracks.add(new Media((S3Utils.downloadFile(v)).toURI().toString())));
        return audioTracks;
    }

    public static class SubmitTextFieldHandler implements EventHandler<KeyEvent> {
        private final ChoiceBox<Address> searchResults;
        private final TextField searchText;

        public SubmitTextFieldHandler(ChoiceBox<Address> searchResults, TextField searchText) {
            this.searchResults = searchResults;
            this.searchText = searchText;
        }

        @Override
        public void handle(KeyEvent keyEvent) {
            if(keyEvent.getCode() == KeyCode.ENTER) {
                if (!searchText.getText().isEmpty()) {
                    FxUtils.generateSearchResultsInChBox(searchResults, searchText.getText());
                } else {
                    System.out.println("Search query is empty.");
                }
            }
        }
    }
}
