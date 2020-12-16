package pl.pkubicki.util;

import fr.dudie.nominatim.model.Address;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.io.IOException;

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
}
