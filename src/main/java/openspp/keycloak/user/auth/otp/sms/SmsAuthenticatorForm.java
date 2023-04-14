package openspp.keycloak.user.auth.otp.sms;

import java.util.List;
import java.util.Locale;

import javax.ws.rs.core.Response;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.theme.Theme;
import org.keycloak.theme.beans.MessageFormatterMethod;

import lombok.extern.slf4j.Slf4j;
import openspp.keycloak.user.auth.beneficiary.oidc.BeneficiaryOIDCAuthenticatorForm;
import openspp.keycloak.user.auth.otp.base.BaseOtpAuthenticatorForm;
import openspp.keycloak.user.auth.otp.sms.service.SmsServiceFactory;
import openspp.keycloak.user.storage.UserAdapter;


@Slf4j
public class SmsAuthenticatorForm extends BaseOtpAuthenticatorForm {
    private static final String TEMPLATE = "sms-otp-form.ftl";

    @Override
    public String getTemplate() {
        return TEMPLATE;
    }

    @Override
    public void sendOtp(AuthenticationFlowContext context, String code, int length, int ttl) {
        AuthenticatorConfigModel config = context.getAuthenticatorConfig();
        KeycloakSession session = context.getSession();
        UserAdapter user = (UserAdapter) context.getUser();

        String phoneNumber = user.getPhoneNumber();
        log.debug("Phone number from user attribute: {}", phoneNumber);

        if (phoneNumber == null) {
            // Get phone number from AuthNote
            phoneNumber = context.getAuthenticationSession().getAuthNote(BeneficiaryOIDCAuthenticatorForm.FIELD_PHONE_NUMBER);
            log.debug("Phone number AuthNote: {}", phoneNumber);
        }

        try {
            Theme theme = session.theme().getTheme(Theme.Type.LOGIN);
            Locale locale = session.getContext().resolveLocale(user);
            MessageFormatterMethod mfm = new MessageFormatterMethod(locale, theme.getMessages(locale));

            String smsAuthText = theme.getMessages(locale).getProperty("otpAuthText");
            List<String> smsTextFormat = List.of(smsAuthText, String.valueOf(length), code, String.valueOf(ttl));
            String smsText = mfm.exec(smsTextFormat).toString();

            SmsServiceFactory.create(context, config.getConfig()).send(phoneNumber, smsText, code, length, ttl);

            challenge(context, null);
        } catch (Exception e) {
            log.error("Cannot send SMS OTP", e);
            context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
                    createForm(context, null).setError("smsAuthSmsNotSent", e.getMessage())
                            .createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
        }
    }

    @Override
    public void handleValidAction(AuthenticationFlowContext context, String code, String ttl) {
        if (Long.parseLong(ttl) < System.currentTimeMillis()) {
            // expired
            log.debug("OTP code expired");
            context.failureChallenge(AuthenticationFlowError.EXPIRED_CODE,
                    createForm(context, null).setAttribute("realm", context.getRealm())
                            .setError("otpAuthCodeExpired").createForm(TEMPLATE));
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
                    createForm(context, null).setAttribute("realm", context.getRealm())
                            .setError("otpAuthCodeInvalid").createForm(TEMPLATE));
        } else if (execution.isConditional() || execution.isAlternative()) {
            context.attempted();
        }
    }

}