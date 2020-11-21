package pl.pkubicki.controllers;

import org.apache.commons.io.IOUtils;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import javafx.stage.Stage;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.S3Client;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;

public class DownloadPoiController {
    private static Region region = Region.EU_NORTH_1;

    public void downloadFile(ActionEvent actionEvent) {
        String bucketName = "siedlce";
        String key = "testaudio.mp3";
        File downloadFile = new File("C:\\Users\\pkubicki\\IntelliJIDEAProjects\\PathPlayer\\downloads\\download" + System.currentTimeMillis() + ".mp3");
        S3Client s3 = S3Client.builder().region(region).build();
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        ResponseBytes<GetObjectResponse> objectAsBytes = s3.getObjectAsBytes(getObjectRequest);
        InputStream inputStream = objectAsBytes.asInputStream();
        try {
            java.nio.file.Files.copy(
                    inputStream,
                    downloadFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }

        IOUtils.closeQuietly(inputStream);

    }
}
