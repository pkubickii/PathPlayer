package pl.pkubicki.util;

import com.javadocmd.simplelatlng.LatLng;
import fr.dudie.nominatim.model.Address;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.media.Media;
import javafx.util.StringConverter;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import pl.pkubicki.controllers.RouteTravelController;

import java.io.IOException;
import java.util.*;

public class FxUtils {
    public static void generateSearchResultsInChBox(ChoiceBox<Address> choiceBox, String query) throws IOException {
        ObservableList<Address> observableList = NominatimUtils.search(query);
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

    public static void getProximityPointsOnRoute(ListView listView) {

    }

    public static void initializeVicinityDistances(ChoiceBox vicinityChBox, ObservableList obListForVicinityDistances) {
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
        obListForVicinityDistances = FXCollections.observableList(vicinityDistances);
        vicinityChBox.getItems().clear();
        vicinityChBox.setItems(obListForVicinityDistances);
        vicinityChBox.setValue(50.0);
    }
    public static LinkedList<Media> getAudioTracks(Set<OWLNamedIndividual> proximityPoints) {
        LinkedList<Media> audioTracks = new LinkedList<>();
        Map<OWLNamedIndividual, String> audioFileNames = OwlUtils.getAudioFileNames(proximityPoints);
        audioFileNames.forEach( (k , v) -> {
            audioTracks.add(new Media((S3Utils.downloadFile(v)).toURI().toString()));
        });
        return audioTracks;
    }

}
