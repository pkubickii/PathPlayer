package pl.pkubicki.repositories;

import org.apache.commons.io.IOUtils;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;

public class S3Repo {
    private static final String bucketName = "siedlce";
    private static final String downloadPath = "./downloads/";
    private static final Region region = Region.EU_NORTH_1;

    public static File downloadFile(String fileName) {
        File file = new File(downloadPath + fileName);
        if (!file.exists()) {
            S3Client s3 = S3Client.builder().region(region).build();
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            ResponseBytes<GetObjectResponse> objectAsBytes = s3.getObjectAsBytes(getObjectRequest);
            InputStream inputStream = objectAsBytes.asInputStream();
            try {
                java.nio.file.Files.copy(
                        inputStream,
                        file.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }

            IOUtils.closeQuietly(inputStream);
        }
        return file;
    }

    public static void uploadFile(File file, String keyName) {
        S3Client s3 = S3Client.builder().region(region).build();
        System.out.format("Uploading %s to the cloud. %n", file.getName());
        s3.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(keyName)
                        .build(),
                RequestBody.fromFile(file));
        System.out.format("File %s uploaded successfully. %n", file.getName());
    }
}
