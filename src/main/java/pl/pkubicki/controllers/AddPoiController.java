package pl.pkubicki.controllers;

import com.javadocmd.simplelatlng.util.LengthUnit;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import com.javadocmd.simplelatlng.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddPoiController {
    private final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    private static Region region = Region.EU_NORTH_1;
    @FXML private Text submitStatus;
    @FXML private Text fileName;
    private static File audioFile;
    private final static double PROXIMITY = 100.0;

    @FXML
    public void selectFile(ActionEvent actionEvent){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open file");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Audio files (*.mp3)", "*.mp3");
        fileChooser.getExtensionFilters().add(extFilter);
        audioFile = fileChooser.showOpenDialog(new Stage());
        if (audioFile != null) {
            fileName.setText(audioFile.getName());
        }
    }

    @FXML
    public static void generateProximityList() {
        LatLng poi0 = new LatLng(52.162995, 22.271528);
        List<LatLng> proxList = new ArrayList<>();
        List<LatLng> allList = Arrays.asList(new LatLng(52.162995, 22.271528));
        allList.forEach( element -> {
                    if (LatLngTool.distance(poi0, element, LengthUnit.METER) < PROXIMITY) {
                        proxList.add(element);
                    }
        });
        System.out.println(proxList.toString());
    }
    @FXML
    private void submitNewPoiButton(ActionEvent actionEvent) {
        S3Client s3 = S3Client.builder().region(region).build();
        String bucket = "siedlce";
//        String stringObjKeyName = "objKey "+ System.currentTimeMillis();
//        String fileObjKeyName = "fileKey " + System.currentTimeMillis();
        if (audioFile.exists()) {

            s3.putObject(PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(audioFile.getName())
                            .build(),
                    RequestBody.fromFile(audioFile));
//                    RequestBody.fromString("Test String Object"));
//            System.out.println("Uploading file: " + audioFile.getName());
//            s3.waiter().waitUntilObjectExists(HeadObjectRequest.builder().bucket(bucket).build());
//            System.out.println(audioFile.getName() +" successfully uploaded.");
//            System.out.printf("%n");
        } else {
            submitStatus.setText("You need to choose a file!");
        }

        submitStatus.setText("Thank you for your support!");
    }

    @FXML
    private void submitNewBucket(ActionEvent actionEvent) {

        S3Client s3 = S3Client.builder().region(region).build();
        String bucket = "bucket" + System.currentTimeMillis();
        String key = "key";

        setupNewBucket(s3, bucket, region);

        System.out.println("Uploading POI...");

        s3.putObject(PutObjectRequest.builder().bucket(bucket).key(key)
                .build(), RequestBody.fromString("FIRST NEW POI!!! WOOHOOO!!!"));

        System.out.println("Upload complete");
        System.out.printf("%n");

        submitStatus.setText("Thank you for your support!");
    }

    public static void setupNewBucket(S3Client s3Client, String bucketName, Region region) {
        try {
            s3Client.createBucket(CreateBucketRequest
                    .builder()
                    .bucket(bucketName)
                    .createBucketConfiguration(
                            CreateBucketConfiguration.builder()
                                    .locationConstraint(region.id())
                                    .build())
                    .build());
            System.out.println("Creating bucket: " + bucketName);
            s3Client.waiter().waitUntilBucketExists(HeadBucketRequest.builder().bucket(bucketName).build());
            System.out.println(bucketName +" is ready.");
            System.out.printf("%n");
        }catch (S3Exception e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }

}
