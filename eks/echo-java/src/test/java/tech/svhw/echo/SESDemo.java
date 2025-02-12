package tech.svhw.echo;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.*;
import com.amazonaws.services.simpleemail.model.*;

public class SESDemo {

    public static void main(String[] args) {
        AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
        SendEmailRequest request = new SendEmailRequest()
                .withSource("PalmPay<noreply@svhw.tech>")
                .withDestination(new Destination().withToAddresses("whuaning@svhw.tech"))
                .withMessage(new Message()
                        .withSubject(new Content().withData("Test email"))
                        .withBody(new Body()
                                .withText(new Content().withData("Hello world!"))))
//                .withConfigurationSetName("cloudwatch")
                ;
        SendEmailResult result = client.sendEmail(request);
        System.out.println(result);
    }
}
