package telran.accounting.configuration;

import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class EmailEncryptionUtils {
    private static final String ALGORITHM = "AES";
    private static final String KEY = "KEY_EMAIL_123456";

    public static String encryptAndEncodeUserId(String email) throws Exception {
        String encryptedEmail = encrypt(email);
        String encodedEmail = org.apache.tomcat.util.codec.binary.Base64.encodeBase64URLSafeString(encryptedEmail.getBytes());
        return encodedEmail;
    }

    public static String decryptAndDecodeUserId(String encodedAndEncryptedEmail) throws Exception {
        byte[] encryptedBytes = Base64.decodeBase64(encodedAndEncryptedEmail);
        String decryptedEmail = decrypt(new String(encryptedBytes));
        return decryptedEmail;
    }

    private static String encrypt(String data) throws Exception {
        SecretKey secretKey = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return new String(encryptedBytes, StandardCharsets.ISO_8859_1);
    }

    private static String decrypt(String encryptedData) throws Exception {
        SecretKey secretKey = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(encryptedData.getBytes(StandardCharsets.ISO_8859_1));
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}
