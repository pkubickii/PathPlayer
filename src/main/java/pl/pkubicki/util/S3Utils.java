package pl.pkubicki.util;

import org.apache.commons.io.IOUtils;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;

public class S3Utils {
    private static final String bucketName = "siedlce";
    private static final String downloadPath = "C:\\Users\\pkubicki\\IntelliJIDEAProjects\\PathPlayer\\downloads\\";
    private static final Region region = Region.EU_NORTH_1;

    public static File downloadFile(String fileName) {
        File file = new File(downloadPath + fileName);
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

        return file;
    }
}
