package openspp.keycloak.user.auth.beneficiary.oidc;

import java.util.List;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import com.google.auto.service.AutoService;


@AutoService(AuthenticatorFactory.class)
public class BeneficiaryOIDCAuthenticatorFactory implements AuthenticatorFactory {
    public static final String PROVIDER_ID = "ben-oidc-authenticator";

    public static final String INT_PHONE_CODE_FIELD = "intPhoneCode";
    public static final String SAMPLE_PHONE_NUMBER_FIELD = "samplePhoneNumber";

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "Beneficiary OIDC Authentication";
    }

    @Override
    public String getReferenceCategory() {
        return "oidc";
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return true;
    }

    @Override
    public String getHelpText() {
        return "Beneficiary OIDC Authenticator.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return List.of(
            // TODO: Make a list of standard international phone code with google/libphonenumber.
            new ProviderConfigProperty(INT_PHONE_CODE_FIELD, "International Country Phone Code", "The internation phone code for the country.",
                    ProviderConfigProperty.STRING_TYPE, "+964"),
            new ProviderConfigProperty(SAMPLE_PHONE_NUMBER_FIELD, "Sample Phone Number", "The sample phone number display as placeholder in the phone number field.",
                    ProviderConfigProperty.STRING_TYPE, "712 345 6789")
        );
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return new BeneficiaryOIDCAuthenticatorForm(session);
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }
}
