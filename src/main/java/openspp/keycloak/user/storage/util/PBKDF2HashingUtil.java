package openspp.keycloak.user.storage.util;

import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PBKDF2HashingUtil {

    /**
     * Reference:
     * https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#SecretKeyFactory
     * and https://www.rfc-editor.org/rfc/rfc8018
     * Odoo res_users using PBKDF2-SHA512 with iterations = 25000, generate and
     * verify with python passlib library.
     * 
     * @param password
     * @param hmac
     * @throws Exception
     */
    public static boolean validatePassword(String password, String hash) throws Exception {
        String[] hashAttrs = hash.split("\\$");
        if (hashAttrs.length < 4) {
            throw new Exception("Invalid password hash");
        }
        String hmac = hashAttrs[1].split("-")[1].toUpperCase();
        int iterations = Integer.parseInt(hashAttrs[2]);
        byte[] salt = Base64.getDecoder().decode(hashAttrs[3].replace(".", "+"));
        int keyLength = 512;
        String computedHash = "";
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmac" + hmac);
        PBEKeySpec spec = new PBEKeySpec(
            password.toCharArray(),
            salt,
            iterations,
            keyLength
        );
        SecretKey secretKey = skf.generateSecret(spec);
        computedHash = Base64.getEncoder().encodeToString(secretKey.getEncoded());
        computedHash = computedHash.replace("+", ".");
        computedHash = computedHash.replaceAll("=+$", "");
        return hashAttrs[4].equals(computedHash);
    }
}
