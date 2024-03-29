package org.openspp.keycloak.user.auth.otp.base;

import java.util.List;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;


public abstract class BaseOtpAuthenticatorFactory implements AuthenticatorFactory {
    public static final String TTL_FIELD = "ttl";
    public static final String RESEND_TIME_FIELD = "resendTime";
    public static final String LENGTH_FIELD = "length";
    public static final String SIMULATION_FIELD = "simulation";
    public static final String SIMULATION_CODE_FIELD = "simulationCode";

    public abstract String getId();

    public abstract String getDisplayType();

    public abstract String getHelpText();

    @Override
    public String getReferenceCategory() {
        return "otp";
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return true;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return List.of(
            new ProviderConfigProperty(LENGTH_FIELD, "Code length", "The number of digits of the generated code.",
                    ProviderConfigProperty.STRING_TYPE, 6),
            new ProviderConfigProperty(TTL_FIELD, "Time-to-live",
                    "The time to live in minutes for the code to be valid.", ProviderConfigProperty.STRING_TYPE,
                    "5"),
            new ProviderConfigProperty(RESEND_TIME_FIELD, "Resend time",
                    "The time to allow user to resend code. The value should be larger or equal to time-to-live value.", ProviderConfigProperty.STRING_TYPE,
                    "5"),
            new ProviderConfigProperty(SIMULATION_FIELD, "Simulation mode",
                    "In simulation mode, the SMS won't be sent, but printed to the server logs",
                    ProviderConfigProperty.BOOLEAN_TYPE, true),
            new ProviderConfigProperty(SIMULATION_CODE_FIELD, "Simulation code",
                    "Code using in simulation mode, if this value is not empty it will be used instead of the random generation code.",
                    ProviderConfigProperty.STRING_TYPE, "111111"));
    }

    public abstract Authenticator create(KeycloakSession session);

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
