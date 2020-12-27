package pl.pkubicki.util;

import fr.dudie.nominatim.model.Address;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.Media;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.reasoner.NodeSet;
import pl.pkubicki.repositories.S3Repo;

import java.io.IOException;
import java.util.*;

public class FxUtils {
    private static final Effect focusEffect = new DropShadow(BlurType.GAUSSIAN, Color.LIGHTGREEN, 8, 0.9, 3, 3);

    public static void generateSearchResultsInChBox(ChoiceBox<Address> choiceBox, String query){
        ObservableList<Address> observableList = FXCollections.emptyObservableList();
        try {
            observableList = NominatimUtils.search(query);
        } catch (IOException e) {
            e.printStackTrace();
        }
        choiceBox.getItems().clear();
        choiceBox.setItems(observableList);
        setSearchResultsChBoxStringConverter(choiceBox);
        choiceBox.getSelectionModel().selectFirst();
        choiceBox.show();
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
        audioFileNames.forEach( (k , v) -> audioTracks.add(new Media((S3Repo.downloadFile(v)).toURI().toString())));
        return audioTracks;
    }

    public static ObservableList<OWLClass> getObRealEstateSubClasses() {
        NodeSet<OWLClass> realEstateSubClasses = OwlUtils.getRealEstateSubClasses();
        List<OWLClass> realEstates = new ArrayList<>();
        realEstateSubClasses.forEach(c -> {
            if (!c.isBottomNode())
            realEstates.add(c.getRepresentativeElement());
        });
        return FXCollections.observableList(realEstates);
    }

    public static void getFocusListener(Node focusedNode, Node nodeLabel) {
        focusedNode.focusedProperty().addListener((ObservableValue<? extends Boolean> obs, Boolean oldVal, Boolean newVal) -> {
            focusEffect(newVal, nodeLabel);
        });
    }

    public static void getFocusListener(Node focusedNode) {
        focusedNode.focusedProperty().addListener((ObservableValue<? extends Boolean> obs, Boolean oldVal, Boolean newVal) -> {
            focusEffect(newVal, focusedNode);
        });
    }

    private static void focusEffect(boolean state, Node node) {
        if(state) {
            if (node instanceof Button) {
                node.getStyleClass().remove("shadow");
                node.setEffect(focusEffect);
            }
            node.setEffect(focusEffect);
        } else {
            if (node instanceof Button) {
                node.getStyleClass().add("shadow");
            }
            node.setEffect(null);
        }
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

    public static class TabTraversalEventHandler implements EventHandler<KeyEvent> {

        @Override
        public void handle(KeyEvent event) {
            KeyCode code = event.getCode();

            if (code == KeyCode.TAB && !event.isShiftDown() && !event.isControlDown()) {
                event.consume();
                Node node = (Node) event.getSource();
                KeyEvent newEvent
                        = new KeyEvent(event.getSource(),
                        event.getTarget(), event.getEventType(),
                        event.getCharacter(), event.getText(),
                        event.getCode(), event.isShiftDown(),
                        true, event.isAltDown(),
                        event.isMetaDown());

                node.fireEvent(newEvent);
            }
        }
    }

}
