package tech.svhw.echo;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;

import java.net.URL;

public class S3PresignDemo {

    public static void main(String[] args) {
        AmazonS3 client = AmazonS3ClientBuilder.standard()
//                .withCredentials(new ProfileCredentialsProvider("lprincewhn"))
                .withRegion(Regions.EU_WEST_3).build();

        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest("presign-test-huaning", "test1");
        URL result = client.generatePresignedUrl(request);
        System.out.println(result.toString());
    }
}
