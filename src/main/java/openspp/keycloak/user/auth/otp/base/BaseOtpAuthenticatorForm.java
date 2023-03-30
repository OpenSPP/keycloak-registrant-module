package openspp.keycloak.user.auth.otp.base;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;

import openspp.keycloak.user.auth.otp.OtpUtilities;


public abstract class BaseOtpAuthenticatorForm implements Authenticator {
    public static final String CODE_FIELD = "code";
    public static final String TTL_FIELD = "ttl";

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        AuthenticatorConfigModel config = context.getAuthenticatorConfig();

        int length = Integer.parseInt(config.getConfig().get("length"));
        int ttl = Integer.parseInt(config.getConfig().get(TTL_FIELD));

        String code = OtpUtilities.makeOtpCode(length);
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        authSession.setAuthNote(CODE_FIELD, code);
        authSession.setAuthNote(TTL_FIELD, Long.toString(System.currentTimeMillis() + (ttl * 1000L)));

        sendOtp(context, code, ttl);
    }

    public abstract void sendOtp(AuthenticationFlowContext context, String code, int ttl);

    @Override
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String enteredCode = formData.getFirst(CODE_FIELD);

        // if (formData.containsKey("resend")) {
        //     resetOtp(context);
        //     challenge(context, null);
        //     return;
        // }

        // if (formData.containsKey("cancel")) {
        //     resetOtp(context);
        //     context.resetFlow();
        //     return;
        // }

        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        String code = authSession.getAuthNote(CODE_FIELD);
        String ttl = authSession.getAuthNote(TTL_FIELD);

        if (code == null || ttl == null) {
            context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
                    context.form().createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
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
