package openspp.keycloak.user.auth.beneficiary.oidc;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.messages.Messages;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class BeneficiaryOIDCAuthenticatorForm implements Authenticator {
    static final String TEMPLATE = "ben-oidc-authenticator-form.ftl";

    public static final String FIELD_UID = "uid";
    public static final String FIELD_HOUSEHOLD_NUMBER = "household_number";
    public static final String FIELD_PHONE_NUMBER = "phone_number";
    public static final String FIELD_OTP = "otp";
    public static final String FIELD_PASSWORD = "password";

    public static final String[] FIELDS = {
        FIELD_UID,
        FIELD_HOUSEHOLD_NUMBER,
        FIELD_PHONE_NUMBER,
        FIELD_OTP,
    };

    private Map<String, String> configValues;

    private AuthenticatorConfigModel config;
    private final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    public BeneficiaryOIDCAuthenticatorForm(KeycloakSession session) {
    }

    private void getConfig(AuthenticationFlowContext context) {
        config = context.getAuthenticatorConfig();
        configValues = config.getConfig();
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        getConfig(context);
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

    private PhoneNumber parsePhoneNumber(AuthenticationFlowContext context, String phoneNumber) throws NumberParseException {
        PhoneNumber pn = null;
        String intPhoneCode = configValues.getOrDefault(BeneficiaryOIDCAuthenticatorFactory.INT_PHONE_CODE_FIELD, null);

        // Pre-process phone number format
        if (intPhoneCode != null && !intPhoneCode.isEmpty()) {
            intPhoneCode = intPhoneCode.replaceAll("[^0-9]+", "");
            String phoneDigits = phoneNumber.replaceAll("[^0-9]+", "");
            
            switch (phoneDigits.length()) {
            case 10:
                phoneNumber = "+" + intPhoneCode + phoneDigits;
                break;
            case 13:
                phoneNumber = "+" + phoneDigits;
                break;
            case 14:
                phoneNumber = "+" + intPhoneCode + phoneDigits.replaceAll("^00\\d{2}", "");
                break;
            default:
                break;
            }
        }

        pn = phoneNumberUtil.parse(phoneNumber, null);

        return pn;
    }

    protected boolean validateForm(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        if (formData.containsKey(FIELD_UID)
                && formData.containsKey(FIELD_HOUSEHOLD_NUMBER)
                && formData.containsKey(FIELD_PHONE_NUMBER)
            ) {

            String uidNumber = formData.getFirst(FIELD_UID);
            String householdNumber = formData.getFirst(FIELD_HOUSEHOLD_NUMBER);
            String phoneNumber = formData.getFirst(FIELD_PHONE_NUMBER);
            String password = formData.getFirst(FIELD_PASSWORD);

            boolean isPhoneNumberValid = false;
            try {
                PhoneNumber pn = parsePhoneNumber(context, phoneNumber);
                phoneNumber = phoneNumberUtil.format(pn, PhoneNumberFormat.E164);
                if (phoneNumberUtil.isValidNumber(pn)) {
                    isPhoneNumberValid = true;
                }
            } catch (Exception e) {
                isPhoneNumberValid = false;
                log.error("Something wrong when trying to validate and format phone number: {} \n {}", phoneNumber, e);
            }

            if (!isPhoneNumberValid) {
                log.error("Invalid phone number: {}", phoneNumber);
                Response challengeResponse = challenge(context, "invalidPhoneNumberMessage", FIELD_PHONE_NUMBER);
                context.forceChallenge(challengeResponse);
                return false;
            }

            context.getAuthenticationSession().setAuthNote(FIELD_UID, uidNumber);
            context.getAuthenticationSession().setAuthNote(FIELD_HOUSEHOLD_NUMBER, householdNumber);
            context.getAuthenticationSession().setAuthNote(FIELD_PHONE_NUMBER, phoneNumber);

            UserModel user = null;
            try {
                user = context.getSession().users().getUserByUsername(context.getRealm(), householdNumber);
                
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
        getConfig(context);
        Response challengeResponse = challenge(context, null, null);
        context.challenge(challengeResponse);
    }

    protected Response challenge(AuthenticationFlowContext context, String error, String field) {
        LoginFormsProvider form = context.form()
                .setExecution(context.getExecution().getId());
        
        MultivaluedMap<String, String> formData = new MultivaluedMapImpl<>();
        Map<String, String> formDataX = new Hashtable<>();
        formDataX.putAll(configValues);
        
        String[] formFields = {
            FIELD_UID,
            FIELD_HOUSEHOLD_NUMBER,
            FIELD_PHONE_NUMBER,
        };
    
        for (int i=0; i<formFields.length; i++) {
            String fieldData = context.getAuthenticationSession().getAuthNote(formFields[i]);
            if (fieldData!= null) {
                // log.info("Field={} Value={}", formFields[i], fieldData);
                // Remove international code from phoneNumber.
                String intPhoneCode = configValues.getOrDefault(BeneficiaryOIDCAuthenticatorFactory.INT_PHONE_CODE_FIELD, null);
                if (intPhoneCode != null && formFields[i] == FIELD_PHONE_NUMBER) {
                    fieldData = fieldData.replace(intPhoneCode, "");
                }
                formDataX.put(formFields[i], fieldData);
                formData.add(formFields[i], fieldData);
            }
        }

        form.setAttribute("formDataX", formDataX);
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
