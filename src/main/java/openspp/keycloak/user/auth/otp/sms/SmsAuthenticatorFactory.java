package openspp.keycloak.user.auth.otp.sms;

import java.util.ArrayList;
import java.util.List;

import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;

import openspp.keycloak.user.auth.otp.base.BaseOtpAuthenticatorFactory;

public class SmsAuthenticatorFactory extends BaseOtpAuthenticatorFactory {
    public static final String PROVIDER_ID = "sms-authenticator";

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
        pcpNew.add(
            new ProviderConfigProperty("senderId", "SenderId",
                    "The sender ID is displayed as the message sender on the receiving device.",
                    ProviderConfigProperty.STRING_TYPE, "Keycloak")
        );

        return pcpNew;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return new SmsAuthenticatorForm();
    }

}