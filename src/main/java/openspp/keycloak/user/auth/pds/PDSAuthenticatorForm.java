package openspp.keycloak.user.auth.pds;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.messages.Messages;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PDSAuthenticatorForm implements Authenticator {

    static final String ID = "pds-authenticator-form";
    static final String TEMPLATE = "pds-authenticator-form.ftl";

    public static final String FIELD_PDS = "pds";
    public static final String FIELD_UID = "uid";
    public static final String FIELD_FAMILY_NUMBER = "family_number";
    public static final String FIELD_PHONE_NUMBER = "phone_number";
    public static final String FIELD_OTP = "otp";
    public static final String FIELD_PASSWORD = "password";

    public static final String[] FIELDS = {
        FIELD_PDS,
        FIELD_UID,
        FIELD_FAMILY_NUMBER,
        FIELD_PHONE_NUMBER,
        FIELD_OTP,
        FIELD_PASSWORD,
    };

    private final KeycloakSession session;

    public PDSAuthenticatorForm(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        if (formData.containsKey("cancel")) {
            context.cancelLogin();
            return;
        }
        if (!validateForm(context, formData)) {
            return;
        }

        context.success();
        // Remove auth notes after validate credentials
        log.debug("AuthNote cleanup.");
        for (int i=0; i<FIELDS.length; i++) {
            context.getAuthenticationSession().removeAuthNote(FIELDS[i]);
        }
    }

    protected boolean validateForm(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        if (formData.containsKey(FIELD_PDS) && formData.containsKey(FIELD_UID)
                && formData.containsKey(FIELD_FAMILY_NUMBER) && formData.containsKey(FIELD_PHONE_NUMBER)) {

            String pdsNumber = formData.getFirst(FIELD_PDS);
            String uidNumber = formData.getFirst(FIELD_UID);
            String familyNumber = formData.getFirst(FIELD_FAMILY_NUMBER);
            String phoneNumber = formData.getFirst(FIELD_PHONE_NUMBER);
            String password = formData.getFirst(FIELD_PASSWORD);

            context.getAuthenticationSession().setAuthNote(FIELD_PDS, pdsNumber);
            context.getAuthenticationSession().setAuthNote(FIELD_UID, uidNumber);
            context.getAuthenticationSession().setAuthNote(FIELD_FAMILY_NUMBER, familyNumber);
            context.getAuthenticationSession().setAuthNote(FIELD_PHONE_NUMBER, phoneNumber);
            context.getAuthenticationSession().setAuthNote(FIELD_PASSWORD, password);

            UserModel user = null;
            try {
                user = context.getSession().users().getUserByUsername(context.getRealm(), familyNumber);
                
                if (user == null) {
                    log.info("User not found.");
                    Response challengeResponse = challenge(context, "invalidCredentials", null);
                    context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challengeResponse);
                    return false;
                }
                
                if (password == null || password.isEmpty()) {
                    log.info("Password is empty.");
                    Response challengeResponse = challenge(context, Messages.INVALID_PASSWORD, FIELD_PASSWORD);
                    context.forceChallenge(challengeResponse);
                    return false;
                }
                if (user.credentialManager().isValid(UserCredentialModel.password(password))) {
                    log.info("Authentication success, credential is valid.");
                    context.setUser(user);
                } else {
                    log.info("Password is incorrect.");
                    Response challengeResponse = challenge(context, Messages.INVALID_PASSWORD, FIELD_PASSWORD);
                    context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challengeResponse);
                    return false;
                }
            } catch (ModelDuplicateException mde) {
                ServicesLogger.LOGGER.modelDuplicateException(mde);
                Response challengeResponse = challenge(context, "invalidCredentials", null);
                context.failureChallenge(AuthenticationFlowError.GENERIC_AUTHENTICATION_ERROR, challengeResponse);
                return false;
            }

            return true;
        } else {
            // invalid
            AuthenticationExecutionModel execution = context.getExecution();
            if (execution.isRequired()) {
                context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS,
                        context.form().setAttribute("realm", context.getRealm())
                                .setError("smsAuthCodeInvalid").createForm(TEMPLATE));
            } else if (execution.isConditional() || execution.isAlternative()) {
                context.attempted();
            }
            return false;
        }
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        Response challengeResponse = challenge(context, null, null);
        context.challenge(challengeResponse);
    }

    protected Response challenge(AuthenticationFlowContext context, String error, String field) {
        LoginFormsProvider form = context.form()
                .setExecution(context.getExecution().getId());
        
        MultivaluedMap<String, String> formData = new MultivaluedMapImpl<>();
        
        for (int i=0; i<FIELDS.length; i++) {
            if (context.getAuthenticationSession().getAuthNote(FIELDS[i]) != null) {
                formData.add(
                    FIELDS[i],
                    context.getAuthenticationSession().getAuthNote(FIELDS[i])
                );
            }
        }

        log.debug("vvvvvvv");
        log.debug(StringUtils.join(", ", formData));
        log.debug("^^^^^^^");
        if (!formData.isEmpty()) {
            form.setFormData(formData);
        }

        if (error != null) {
            if (field != null) {
                form.addError(new FormMessage(field, error));
            } else {
                form.setError(error);
            }
        }
        return form.createForm(TEMPLATE);
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    }

    @Override
    public void close() {
    }

}
