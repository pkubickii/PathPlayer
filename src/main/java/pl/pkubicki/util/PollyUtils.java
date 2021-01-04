package pl.pkubicki.util;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

public class PollyUtils {
    private static final Region region = Region.EU_NORTH_1;
    private static AdvancedPlayer player;
    private static AtomicBoolean isPlaying = new AtomicBoolean(false);

    public static void play(String text) {
        if (isPlaying()) {
            player.stop();
            player.close();
        }
        PollyClient polly = PollyClient.builder().region(region).build();
        talkPolly(polly, text);
        polly.close();
    }
    public static void talkPolly(PollyClient polly, String text) {
        try {
            DescribeVoicesRequest describeVoiceRequest = DescribeVoicesRequest.builder()
                    .engine("standard")
                    .languageCode("pl-PL")
                    .build();

            DescribeVoicesResponse describeVoicesResult = polly.describeVoices(describeVoiceRequest);
            Voice voice = describeVoicesResult.voices().get(0);

            InputStream stream = synthesize(polly, text, voice, OutputFormat.MP3);

            player = new AdvancedPlayer(stream, javazoom.jl.player.FactoryRegistry.systemRegistry().createAudioDevice());

            player.setPlayBackListener(new PlaybackListener() {

                public void playbackStarted(PlaybackEvent evt) {
                    System.out.println("Playback started");
                    System.out.println(text);
                    isPlaying.getAndSet(true);
                }

                public void playbackFinished(PlaybackEvent evt) {
                    isPlaying.getAndSet(false);
                    System.out.println("Playback finished");
                }
            });

            player.play();
        } catch (PollyException | JavaLayerException | IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    public static InputStream synthesize(PollyClient polly, String text, Voice voice, OutputFormat format) throws IOException {

        SynthesizeSpeechRequest synthReq = SynthesizeSpeechRequest.builder()
                .text(text)
                .voiceId(voice.id())
                .outputFormat(format)
                .build();

        ResponseInputStream<SynthesizeSpeechResponse> synthRes = polly.synthesizeSpeech(synthReq);
        return synthRes;
    }

    public static boolean isPlaying() {
        return isPlaying.get();
    }
}
