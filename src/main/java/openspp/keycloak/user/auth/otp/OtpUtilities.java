package openspp.keycloak.user.auth.otp;

import org.keycloak.common.util.SecretGenerator;

public class OtpUtilities {

    /**
     * Simple method to make OTP code with random string
     * 
     * @param length
     * @return
     */
    public static String makeOtpCode(int length) {
        return SecretGenerator.getInstance().randomString(length, SecretGenerator.DIGITS);
    }
}
