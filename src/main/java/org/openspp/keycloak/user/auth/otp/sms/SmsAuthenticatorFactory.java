package org.openspp.keycloak.user.auth.otp.sms;

import java.util.ArrayList;
import java.util.List;

import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;
import org.openspp.keycloak.user.auth.otp.base.BaseOtpAuthenticatorFactory;

import com.google.auto.service.AutoService;


@AutoService(AuthenticatorFactory.class)
public class SmsAuthenticatorFactory extends BaseOtpAuthenticatorFactory {
    public static final String PROVIDER_ID = "sms-authenticator";
    public static final String SENDER_ID_FIELD = "senderId";
    public static final String SIMULATION_EMAIL_FIELD = "simulationEmail";
    public static final String AWS_TOPIC_ARN_FIELD = "topicArn";

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
    
    @Override
    public String getDisplayType() {
        return "OTP SMS Authentication";
    }

    @Override
    public String getHelpText() {
        return "Validates an OTP code sent via SMS.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        List<ProviderConfigProperty> pcp = super.getConfigProperties();
        List<ProviderConfigProperty> pcpNew = new ArrayList<ProviderConfigProperty>();
        pcpNew.addAll(pcp);
        pcpNew.add(0,
            new ProviderConfigProperty(SENDER_ID_FIELD, "Sender ID",
                    "The sender ID is displayed as the message sender on the receiving device.",
                    ProviderConfigProperty.STRING_TYPE, "OpenSPP")
        );
        pcpNew.add(1,
            new ProviderConfigProperty(AWS_TOPIC_ARN_FIELD, "AWS SNS Topic ARN",
                    "The AWS SNS Topic ARN URI using for SMS service.",
                    ProviderConfigProperty.STRING_TYPE, "")
        );
        pcpNew.add(
            new ProviderConfigProperty(SIMULATION_EMAIL_FIELD, "Simulation email",
                    "The email to receive OTP code in SIMULATION mode.",
                    ProviderConfigProperty.STRING_TYPE, "")
        );

        return pcpNew;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return new SmsAuthenticatorForm();
    }

}