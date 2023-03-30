package openspp.keycloak.user.auth.otp.sms;

import java.util.Locale;

import javax.ws.rs.core.Response;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.theme.Theme;

import lombok.extern.slf4j.Slf4j;
import openspp.keycloak.user.auth.otp.base.BaseOtpAuthenticatorForm;
import openspp.keycloak.user.auth.otp.sms.service.SmsServiceFactory;


@Slf4j
public class SmsAuthenticatorForm extends BaseOtpAuthenticatorForm {
    private static final String TEMPLATE = "sms-otp-form.ftl";

    @Override
    public void sendOtp(AuthenticationFlowContext context, String code, int ttl) {
        AuthenticatorConfigModel config = context.getAuthenticatorConfig();
        KeycloakSession session = context.getSession();
        UserModel user = context.getUser();

        String mobileNumber = user.getFirstAttribute("mobile_number");

        try {
            Theme theme = session.theme().getTheme(Theme.Type.LOGIN);
            Locale locale = session.getContext().resolveLocale(user);
            String smsAuthText = theme.getMessages(locale).getProperty("otpAuthText");
            String smsText = String.format(smsAuthText, code, Math.floorDiv(ttl, 60));

            SmsServiceFactory.create(context, config.getConfig()).send(mobileNumber, smsText, code, ttl);

            context.challenge(context.form().setAttribute("realm", context.getRealm()).createForm(TEMPLATE));
        } catch (Exception e) {
            log.error("Cannot send SMS OTP", e);
            context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
                    context.form().setError("smsAuthSmsNotSent", e.getMessage())
                            .createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
        }
    }

    @Override
    public void handleValidAction(AuthenticationFlowContext context, String code, String ttl) {
        if (Long.parseLong(ttl) < System.currentTimeMillis()) {
            // expired
            log.debug("OTP code expired");
            context.failureChallenge(AuthenticationFlowError.EXPIRED_CODE,
                    context.form().setError("otpAuthCodeExpired").createErrorPage(Response.Status.BAD_REQUEST));
        } else {
            // valid
            log.debug("OTP success");
            context.success();
        }
    }

    @Override
    public void handleInvalidAction(AuthenticationFlowContext context, String code, String ttl) {
        AuthenticationExecutionModel execution = context.getExecution();
        if (execution.isRequired()) {
            log.debug("OTP code is invalid");
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS,
                    context.form().setAttribute("realm", context.getRealm())
                            .setError("otpAuthCodeInvalid").createForm(TEMPLATE));
        } else if (execution.isConditional() || execution.isAlternative()) {
            context.attempted();
        }
    }

}