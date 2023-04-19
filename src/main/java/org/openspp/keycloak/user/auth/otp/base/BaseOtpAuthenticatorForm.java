package org.openspp.keycloak.user.auth.otp.base;

import java.util.Hashtable;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.openspp.keycloak.user.auth.otp.OtpUtilities;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public abstract class BaseOtpAuthenticatorForm implements Authenticator {
    public static final String CODE_FIELD = "code";
    public Map<String, String> configValues;

    public AuthenticatorConfigModel config;

    public void getConfig(AuthenticationFlowContext context) {
        config = context.getAuthenticatorConfig();
        configValues = config.getConfig();
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        getConfig(context);
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        AuthenticatorConfigModel config = context.getAuthenticatorConfig();

        String simulationCode = config.getConfig().get(BaseOtpAuthenticatorFactory.SIMULATION_CODE_FIELD);
        Boolean simulationMode = Boolean.parseBoolean(config.getConfig().getOrDefault(BaseOtpAuthenticatorFactory.SIMULATION_FIELD, "false"));
        int length = Integer.parseInt(config.getConfig().get(BaseOtpAuthenticatorFactory.LENGTH_FIELD));
        int ttl = Integer.parseInt(config.getConfig().get(BaseOtpAuthenticatorFactory.TTL_FIELD));
        int resendTime = Integer.parseInt(config.getConfig().get(BaseOtpAuthenticatorFactory.RESEND_TIME_FIELD));

        if (authSession.getAuthNote(CODE_FIELD) != null) {
            log.info("OTP code already sent, skipping");
            challenge(context, null);
            return;
        }
        String code = simulationCode;
        if (!simulationMode || code == null || code.isEmpty()) {
            code = OtpUtilities.makeOtpCode(length);
        }
        authSession.setAuthNote(CODE_FIELD, code);
        authSession.setAuthNote(BaseOtpAuthenticatorFactory.LENGTH_FIELD, String.valueOf(length));
        authSession.setAuthNote(BaseOtpAuthenticatorFactory.TTL_FIELD, String.valueOf(System.currentTimeMillis() + (ttl * 60 * 1000L)));
        authSession.setAuthNote(BaseOtpAuthenticatorFactory.RESEND_TIME_FIELD, String.valueOf(System.currentTimeMillis() + (resendTime * 60 * 1000L)));

        sendOtp(context, code, length, ttl);
    }

    public abstract void sendOtp(AuthenticationFlowContext context, String code, int length, int ttl);

    @Override
    public void action(AuthenticationFlowContext context) {
        getConfig(context);
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String enteredCode = formData.getFirst(CODE_FIELD).trim();

        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        String code = authSession.getAuthNote(CODE_FIELD);
        String ttl = authSession.getAuthNote(BaseOtpAuthenticatorFactory.TTL_FIELD);
        String resendTime = authSession.getAuthNote(BaseOtpAuthenticatorFactory.RESEND_TIME_FIELD);

        if (formData.containsKey("resend")) {
            if (Long.parseLong(resendTime) > System.currentTimeMillis()) {
                log.warn("Resend OTP action is not allowed. Available in {}ms, skipping", Long.parseLong(resendTime) - System.currentTimeMillis());
                challenge(context, "invalidActionResendOtp");
                return;
            }
            resetOtp(context);
            authenticate(context);
            challenge(context, null);
            return;
        }

        if (formData.containsKey("cancel")) {
            resetOtp(context);
            context.resetFlow();
            return;
        }
        if (code == null || ttl == null) {
            // Code or ttl is null, reset flow
            // context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
            //         context.form().createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
            context.resetFlow();
            return;
        }

        boolean isValid = enteredCode.equals(code);
        if (isValid) {
            handleValidAction(context, code, ttl);
        } else {
            handleInvalidAction(context, code, ttl);
        }
    }

    public void resetOtp(AuthenticationFlowContext context) {
        context.getAuthenticationSession().removeAuthNote(CODE_FIELD);
    }

    public abstract void handleValidAction(AuthenticationFlowContext context, String code, String ttl);
    public abstract void handleInvalidAction(AuthenticationFlowContext context, String code, String ttl);

    public abstract String getTemplate();

    public LoginFormsProvider createForm(AuthenticationFlowContext context, String errorMessage) {
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        LoginFormsProvider form = context.form().setExecution(context.getExecution().getId());
        if (errorMessage != null) {
            form.setError(errorMessage);
        }

        String ttl = authSession.getAuthNote(BaseOtpAuthenticatorFactory.TTL_FIELD);
        String resendTime = authSession.getAuthNote(BaseOtpAuthenticatorFactory.RESEND_TIME_FIELD);
        String length = authSession.getAuthNote(BaseOtpAuthenticatorFactory.LENGTH_FIELD);
        String resendOTPStatus = "";

        if (Long.parseLong(resendTime) > System.currentTimeMillis()) {
            resendOTPStatus = "disabled";
        }

        Map<String, String> formDataX = new Hashtable<>();
        formDataX.putAll(configValues);
        formDataX.put(BaseOtpAuthenticatorFactory.TTL_FIELD, ttl);
        formDataX.put(BaseOtpAuthenticatorFactory.LENGTH_FIELD, length);
        formDataX.put(BaseOtpAuthenticatorFactory.RESEND_TIME_FIELD, resendTime);
        formDataX.put("resendOTPStatus", resendOTPStatus);

        form.setAttribute("formDataX", formDataX);

        return form;
    }

    public void challenge(AuthenticationFlowContext context, String errorMessage) {
        LoginFormsProvider form = createForm(context, errorMessage);
        Response response = form.createForm(getTemplate());
        context.challenge(response);
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

}
