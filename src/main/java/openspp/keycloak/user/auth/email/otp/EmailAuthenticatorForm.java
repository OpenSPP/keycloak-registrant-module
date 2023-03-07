package openspp.keycloak.user.auth.email.otp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.AuthenticationFlowException;
import org.keycloak.authentication.Authenticator;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.messages.Messages;

import lombok.extern.jbosslog.JBossLog;

@JBossLog
public class EmailAuthenticatorForm implements Authenticator {

    static final String ID = "email-otp-form";

    public static final String EMAIL_OTP = "emailOTP";

    private final KeycloakSession session;

    public EmailAuthenticatorForm(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        challenge(context, null);
    }

    private void challenge(AuthenticationFlowContext context, FormMessage errorMessage) {

        generateAndSendOTPEmail(context);

        LoginFormsProvider form = context.form().setExecution(context.getExecution().getId());
        if (errorMessage != null) {
            form.setErrors(List.of(errorMessage));
        }

        Response response = form.createForm("email-otp-form.ftl");
        context.challenge(response);
    }

    private void generateAndSendOTPEmail(AuthenticationFlowContext context) {

        if (context.getAuthenticationSession().getAuthNote(EMAIL_OTP) != null) {
            // skip sending email otp
            return;
        }

        int emailOTP = ThreadLocalRandom.current().nextInt(99999999);
        sendEmailWithOTP(context.getRealm(), context.getUser(), String.valueOf(emailOTP));
        context.getAuthenticationSession().setAuthNote(EMAIL_OTP, Integer.toString(emailOTP));
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest()
                .getDecodedFormParameters();
        if (formData.containsKey("resend")) {
            resetOTPEmail(context);
            challenge(context, null);
            return;
        }

        if (formData.containsKey("cancel")) {
            resetOTPEmail(context);
            context.resetFlow();
            return;
        }

        int givenEmailOTP = Integer.parseInt(formData.getFirst(EMAIL_OTP));
        boolean valid = validateOTP(context, givenEmailOTP);
        if (!valid) {
            context.getEvent().error(Errors.INVALID_USER_CREDENTIALS);
            challenge(context, new FormMessage(Messages.INVALID_ACCESS_CODE));
            return;
        }

        resetOTPEmail(context);
        context.success();
    }

    private void resetOTPEmail(AuthenticationFlowContext context) {
        context.getAuthenticationSession().removeAuthNote(EMAIL_OTP);
    }

    private boolean validateOTP(AuthenticationFlowContext context, int givenOTP) {
        int emailOTP = Integer.parseInt(context.getAuthenticationSession().getAuthNote(EMAIL_OTP));
        return givenOTP == emailOTP;
    }

    @Override
    public boolean requiresUser() {
        return true;
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

    private void sendEmailWithOTP(RealmModel realm, UserModel user, String otp) {
        if (user.getEmail() == null) {
            log.warnf("Could not send OTP email due to missing email. realm=%s user=%s", realm.getId(),
                    user.getUsername());
            throw new AuthenticationFlowException(AuthenticationFlowError.INVALID_USER);
        }

        Map<String, Object> mailBodyAttributes = new HashMap<>();
        mailBodyAttributes.put("username", user.getUsername());
        mailBodyAttributes.put("otp", otp);

        String realmName = realm.getDisplayName() != null ? realm.getDisplayName() : realm.getName();
        List<Object> subjectParams = List.of(realmName);
        try {
            EmailTemplateProvider emailProvider = session.getProvider(EmailTemplateProvider.class);
            emailProvider.setRealm(realm);
            emailProvider.setUser(user);
            // Don't forget to add the welcome-email.ftl (html and text) template to your
            // theme.
            emailProvider.send("emailOTPSubject", subjectParams, "otp-email.ftl", mailBodyAttributes);
        } catch (EmailException eex) {
            log.errorf(eex, "Failed to send OTP email. realm=%s user=%s", realm.getId(), user.getUsername());
        }
    }
}
