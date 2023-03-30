package openspp.keycloak.user.auth.otp.email;

import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.KeycloakSession;

import com.google.auto.service.AutoService;

import openspp.keycloak.user.auth.otp.base.BaseOtpAuthenticatorFactory;

@AutoService(AuthenticatorFactory.class)
public class EmailAuthenticatorFactory extends BaseOtpAuthenticatorFactory {
    public static final String PROVIDER_ID = "email-authenticator";

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "OTP Email Authentication";
    }

    @Override
    public String getHelpText() {
        return "Validates an OTP code sent via Email.";
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return new EmailAuthenticatorForm();
    }
}
