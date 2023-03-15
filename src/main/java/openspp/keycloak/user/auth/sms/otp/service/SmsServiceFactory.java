package openspp.keycloak.user.auth.sms.otp.service;

import org.jboss.logging.Logger;

import java.util.Map;

public class SmsServiceFactory {

    private static final Logger LOG = Logger.getLogger(SmsServiceFactory.class);

    public static SmsService create(Map<String, String> config) {
        if (Boolean.parseBoolean(config.getOrDefault("simulation", "false"))) {
            return (phoneNumber, message) -> LOG.warn(String
                    .format("***** SIMULATION MODE ***** Sending SMS OTP to %s with text: %s", phoneNumber, message));
        } else {
            return new AwsSmsService(config);
        }
    }

}