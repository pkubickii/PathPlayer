package pl.pkubicki.util;

import javafx.collections.ObservableList;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.util.LinkedList;

public class MediaUtils {
    private static Media media;

    public static void playAudioFile(File file) {
        media = new Media(file.toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.play();
    }
    public static void play(Media audioFile) {
        media = audioFile;
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.play();
    }
    public static void play(LinkedList<Media> audioList) {
        if (audioList.isEmpty())
            return;
        media = audioList.poll();
        MediaPlayer player = new MediaPlayer(media);
        player.setOnEndOfMedia(() -> {
            MediaUtils.play(audioList);
        });
        player.play();
    }
    public static void play(ObservableList<Media> observableList) {
        if (observableList.isEmpty())
            return;
        media = observableList.get(0);
        MediaPlayer player = new MediaPlayer(media);
        player.setOnEndOfMedia(() -> {
            observableList.remove(0);
            MediaUtils.play(observableList);
        });
        player.play();
    }
}
