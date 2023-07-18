package org.openspp.keycloak.user.auth.otp.sms.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.email.EmailTemplateProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.openspp.keycloak.user.auth.otp.base.BaseOtpAuthenticatorFactory;
import org.openspp.keycloak.user.auth.otp.base.BaseOtpAuthenticatorForm;
import org.openspp.keycloak.user.auth.otp.sms.SmsAuthenticatorFactory;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class SmsServiceFactory {

    public static SmsService create(AuthenticationFlowContext context, Map<String, String> config) {
        if (Boolean.parseBoolean(config.getOrDefault(BaseOtpAuthenticatorFactory.SIMULATION_FIELD, "false"))) {
            String email = config.get(SmsAuthenticatorFactory.SIMULATION_EMAIL_FIELD);
            KeycloakSession session = context.getSession();
            RealmModel realm = context.getRealm();
            String realmName = realm.getDisplayName() != null ? realm.getDisplayName() : realm.getName();
            return (phoneNumber, message, code, length, ttl) -> {
                log.warn("v".repeat(80));
                log.warn(
                    "SIMULATION MODE: Sending SMS OTP to {} with text: {}",
                    phoneNumber,
                    message
                );
                log.warn("^".repeat(80));
                if (email != null && !email.isEmpty()) {
                    List<Object> subjectParams = List.of(realmName);
                    Map<String, Object> mailBodyAttributes = new HashMap<>();
                    mailBodyAttributes.put(BaseOtpAuthenticatorFactory.LENGTH_FIELD, length);
                    mailBodyAttributes.put(BaseOtpAuthenticatorForm.CODE_FIELD, code);
                    mailBodyAttributes.put(BaseOtpAuthenticatorFactory.TTL_FIELD, ttl);
                    EmailTemplateProvider emailProvider = session.getProvider(EmailTemplateProvider.class);
                    emailProvider.setRealm(realm);
                    emailProvider.setUser(context.getUser());
                    emailProvider.setAttribute(realmName, realmName);
                    context.getUser().setEmail(email);
                    try {
                        emailProvider.send(
                            "emailOTPSubject",
                            subjectParams,
                            "otp-simulation-email.ftl",
                            mailBodyAttributes
                        );
                    } catch (Exception e) {
                        log.error(
                            "Failed to send simulation OTP email. realm={} user={}",
                            realm.getId(),
                            context.getUser().getUsername(),
                            e
                        );
                    }
                }
            };
        } else {
            return new AwsSmsService(config);
        }
    }

}