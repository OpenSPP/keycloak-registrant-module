package org.openspp.keycloak.user.auth.otp.sms.service;

public interface SmsService {
    void send(String phoneNumber, String message, String code, int length, int ttl);
}
