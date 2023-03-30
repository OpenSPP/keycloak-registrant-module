package openspp.keycloak.user.auth.otp.email;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.AuthenticationFlowException;
import org.keycloak.email.EmailException;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import lombok.extern.jbosslog.JBossLog;
import openspp.keycloak.user.auth.otp.base.BaseOtpAuthenticatorForm;

@JBossLog
public class EmailAuthenticatorForm extends BaseOtpAuthenticatorForm {
    static final String TEMPLATE = "email-otp-form.ftl";

    public static final String EMAIL_OTP = "emailOTP";

    @Override
    public void sendOtp(AuthenticationFlowContext context, String code, int ttl) {
        KeycloakSession session = context.getSession();
        RealmModel realm = context.getRealm();
        UserModel user = context.getUser();
        if (user.getEmail() == null) {
            log.warnf("Could not send OTP email due to missing email. realm=%s user=%s", realm.getId(),
                    user.getUsername());
            throw new AuthenticationFlowException(AuthenticationFlowError.INVALID_USER);
        }

        Map<String, Object> mailBodyAttributes = new HashMap<>();
        mailBodyAttributes.put("username", user.getUsername());
        mailBodyAttributes.put(CODE_FIELD, code);
        mailBodyAttributes.put(TTL_FIELD, Math.floorDiv(ttl, 60));

        String realmName = realm.getDisplayName() != null ? realm.getDisplayName() : realm.getName();
        List<Object> subjectParams = List.of(realmName);
        try {
            EmailTemplateProvider emailProvider = session.getProvider(EmailTemplateProvider.class);
            emailProvider.setRealm(realm);
            emailProvider.setUser(user);
            emailProvider.send("emailOTPSubject", subjectParams, "otp-email.ftl", mailBodyAttributes);
            context.challenge(context.form().setAttribute("realm", context.getRealm()).createForm(TEMPLATE));
        } catch (EmailException e) {
            log.errorf(e, "Failed to send OTP email. realm=%s user=%s", realm.getId(), user.getUsername());
            context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
                    context.form().setError("emailAuthEmailNotSent", e.getMessage())
                            .createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
        }
    }

    @Override
    public void handleValidAction(AuthenticationFlowContext context, String code, String ttl) {
        if (Long.parseLong(ttl) < System.currentTimeMillis()) {
            // expired
            context.failureChallenge(AuthenticationFlowError.EXPIRED_CODE,
                    context.form().setError("otpAuthCodeExpired").createErrorPage(Response.Status.BAD_REQUEST));
        } else {
            // valid
            context.success();
        }
    }

    @Override
    public void handleInvalidAction(AuthenticationFlowContext context, String code, String ttl) {
        AuthenticationExecutionModel execution = context.getExecution();
        if (execution.isRequired()) {
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS,
                    context.form().setAttribute("realm", context.getRealm())
                            .setError("otpAuthCodeInvalid").createForm(TEMPLATE));
        } else if (execution.isConditional() || execution.isAlternative()) {
            context.attempted();
        }
    }
}
