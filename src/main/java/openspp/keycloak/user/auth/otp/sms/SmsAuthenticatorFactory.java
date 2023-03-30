package openspp.keycloak.user.auth.otp.sms;

import java.util.ArrayList;
import java.util.List;

import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.provider.ProviderConfigProperty;

import com.google.auto.service.AutoService;

import openspp.keycloak.user.auth.otp.base.BaseOtpAuthenticatorFactory;


@AutoService(AuthenticatorFactory.class)
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
            new ProviderConfigProperty("senderId", "Sender ID",
                    "The sender ID is displayed as the message sender on the receiving device.",
                    ProviderConfigProperty.STRING_TYPE, "OpenSPP")
        );
        pcpNew.add(
            new ProviderConfigProperty("simulationEmail", "Simulation email",
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