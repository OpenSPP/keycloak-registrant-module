package openspp.keycloak.user.auth.otp.sms.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import openspp.keycloak.user.auth.otp.sms.SmsAuthenticatorFactory;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;


@Slf4j
public class AwsSmsService implements SmsService {

    private static final SnsClient sns = SnsClient.create();

    private final String senderId;
    private final String topicArn;

    AwsSmsService(Map<String, String> config) {
        senderId = config.get(SmsAuthenticatorFactory.SENDER_ID_FIELD);
        topicArn = config.get(SmsAuthenticatorFactory.AWS_TOPIC_ARN_FIELD);
    }

    @Override
    public void send(String phoneNumber, String message, String code, int ttl) {
        Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
        messageAttributes.put(
            "AWS.SNS.SMS.SenderID",
            MessageAttributeValue.builder().stringValue(senderId).dataType("String").build()
        );
        messageAttributes.put(
            "AWS.SNS.SMS.SMSType",
            MessageAttributeValue.builder().stringValue("Transactional").dataType("String").build()
        );

        String dedupId = UUID.randomUUID().toString();

        PublishRequest request = PublishRequest.builder()
            .message(message)
            .phoneNumber(phoneNumber)
            .topicArn(topicArn)
            .messageGroupId(senderId)
            .messageDeduplicationId(dedupId)
            .messageAttributes(messageAttributes)
            .build();

        PublishResponse response = sns.publish(request);
        log.info(
            "{} message sent. Status was {}",
            response.messageId(),
            response.sdkHttpResponse().statusCode()
        );
    }
}