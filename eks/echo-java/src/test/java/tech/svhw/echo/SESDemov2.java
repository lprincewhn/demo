package tech.svhw.echo;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SesException;

public class SESDemov2 {
    public static void main(String[] args) {
        String fromEmail = "PalmPay<noreply@svhw.tech>";
        String toEmail = "whuaning@amazon.com";
        String subject = "Test Email";
        String bodyText = "This is a test email sent using the AWS Java SDK v2.";

        try {
            // Create an SES client
            SesClient sesClient = createSesClient();

            // Create the email request
            SendEmailRequest request = SendEmailRequest.builder()
                    .destination(Destination.builder()
                            .toAddresses(toEmail)
                            .build())
                    .message(Message.builder()
                            .subject(Content.builder()
                                    .data(subject)
                                    .build())
                            .body(Body.builder()
                                    .text(Content.builder()
                                            .data(bodyText)
                                            .build())
                                    .build())
                            .build())
                    .source(fromEmail)
                    .build();

            // Send the email
            sesClient.sendEmail(request);
            System.out.println("Email sent successfully!");
        } catch (SesException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }

    private static SesClient createSesClient() {
        // Replace with your AWS credentials
        String accessKey = "xxxx";
        String secretKey = "xxxx";

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);

        return SesClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(Region.US_EAST_1)
                .build();
    }
}
