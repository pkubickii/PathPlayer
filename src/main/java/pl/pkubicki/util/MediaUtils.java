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
    public static void play(Media audioTrack) {
        media = audioTrack;
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.play();
    }
    public static void play(LinkedList<Media> audioTracks) {
        if (audioTracks.isEmpty())
            return;
        media = audioTracks.poll();
        MediaPlayer player = new MediaPlayer(media);
        player.setOnEndOfMedia(() -> {
            MediaUtils.play(audioTracks);
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
    public static void pause(MediaPlayer player) {
        if(player != null) {
            if(player.getStatus() == MediaPlayer.Status.PLAYING) {
                    player.pause();
                } else if (player.getStatus() == MediaPlayer.Status.PAUSED){
                    player.play();
                }
        }
    }

    public static void stop(MediaPlayer player) {
        if(player != null) {
            if (player.getStatus() != MediaPlayer.Status.STOPPED) {
                player.stop();
            }
        }
    }
}
