package pl.pkubicki.util;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;


import java.io.*;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class S3UtilsTest {
    private static final String bucketName = "siedlce";
    private static final String downloadPath = "C:\\Users\\pkubicki\\IntelliJIDEAProjects\\PathPlayer\\downloads\\";
    private static final Region region = Region.EU_NORTH_1;
    private static final String fileName = "testaudio.mp3";
    private static Properties CREDS = new Properties();
    private static final String CRED_PATH = "target/credentials";

    @BeforeEach
    void setUp(){
        try {
            InputStream in = new FileInputStream(CRED_PATH);
            CREDS.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void s3CredTest() {
        File file = new File(downloadPath + fileName);
        AwsCredentials credentials = AwsBasicCredentials.create(CREDS.getProperty("aws_access_key_id"), CREDS.getProperty("aws_secret_access_key"));
        S3Client s3 = S3Client.builder().region(region).credentialsProvider(StaticCredentialsProvider.create(credentials)).build();
        software.amazon.awssdk.services.s3.model.GetObjectRequest getObjectRequest = GetObjectRequest.builder()
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


    public void uploadToBucket() {

        S3Client s3 = S3Client.builder().region(region).build();
        String bucket = "bucket" + System.currentTimeMillis();
        String key = "key";

        setupNewBucket(s3, bucket, region);

        System.out.println("Uploading POI...");

        s3.putObject(PutObjectRequest.builder().bucket(bucket).key(key)
                .build(), RequestBody.fromString("new key in bucket"));

        System.out.println("Upload complete");
        System.out.printf("%n");

 //       submitStatus.setText("Thank you for your support!");
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