package pl.pkubicki.controllers;

import javafx.event.ActionEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import pl.pkubicki.util.MediaUtils;
import pl.pkubicki.util.S3Utils;
import software.amazon.awssdk.regions.Region;

import java.io.File;

public class DownloadPoiController {

    public void downloadFile(ActionEvent actionEvent) throws InterruptedException {
        File file = S3Utils.downloadFile("testaudio.mp3");
        MediaUtils.playAudio(file);
    }
}
