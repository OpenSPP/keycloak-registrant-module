package openspp.keycloak.user.auth.sms.otp.service;

public interface SmsService {
    void send(String phoneNumber, String message);
}