package pl.pkubicki.util;

import fr.dudie.nominatim.model.Address;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import org.semanticweb.owlapi.model.OWLClass;

public class AudioUtils {

    public static void initializeChoiceBoxAudioRead(ChoiceBox<?> cB, String onEmptyMsg) {
        cB.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            if(code == KeyCode.F2) {
                if (!cB.getSelectionModel().isEmpty()) {
                    if(cB.getValue() instanceof Address) {
                        Address address = (Address) cB.getValue();
                        String value = address.getDisplayName();
                        new Thread(() -> PollyUtils.play(value)).start();
                    } else if (cB.getValue() instanceof OWLClass) {
                        OWLClass realEstate = (OWLClass) cB.getValue();
                        String value = realEstate.getIRI().getRemainder().get();
                        new Thread(() -> PollyUtils.play(value)).start();
                    } else {
                        String value = cB.getValue().toString();
                        new Thread(() -> PollyUtils.play(value)).start();
                    }
                } else {
                    new Thread(() -> PollyUtils.play(onEmptyMsg)).start();
                }
            }
        });
    }

    public static void initializeTextAudioRead(Node textNode, String onEmptyMsg) {
        textNode.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            if(code == KeyCode.F2) {
                if (textNode instanceof TextField) {
                    TextField node = (TextField) textNode;
                    if(!node.getText().isEmpty()) {
                        String value = node.getText();
                        new Thread(() -> PollyUtils.play(value)).start();
                    } else {
                        new Thread(() -> PollyUtils.play(onEmptyMsg)).start();
                    }
                } else if (textNode instanceof TextArea) {
                    TextArea node = (TextArea) textNode;
                    if(!node.getText().isEmpty()) {
                        String value = node.getText();
                        new Thread(() -> PollyUtils.play(value)).start();
                    } else {
                        new Thread(() -> PollyUtils.play(onEmptyMsg)).start();
                    }
                } else {
                    System.out.println("Wrong node. TextField or TextArea only.");
                }
            }
        });
    }

}
