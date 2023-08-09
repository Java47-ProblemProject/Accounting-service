package telran.accounting.configuration;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class EmailEncryptionUtils {
    private static final String ALGORITHM = "AES";
    private static final String KEY = "KEY_EMAIL_123456";
    public static String encryptAndEncodeUserId(String userId) throws Exception {
        String encodedUserId = URLEncoder.encode(userId, StandardCharsets.UTF_8);
        String encryptedUserId = encrypt(encodedUserId);
        return encryptedUserId;
    }

    public static String decryptAndDecodeUserId(String encryptedAndEncodedUserId) throws Exception {
        String decryptedUserId = decrypt(encryptedAndEncodedUserId);
        String decodedUserId = URLDecoder.decode(decryptedUserId, StandardCharsets.UTF_8);
        return decodedUserId;
    }

    private static String encrypt(String data) throws Exception {
        SecretKey secretKey = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    private static String decrypt(String encryptedData) throws Exception {
        SecretKey secretKey = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }





//    private static final String ALGORITHM = "AES";
//    private static final String KEY = "KEY_EMAIL_123456";
//
//    public static String encryptEmail(String email) throws Exception {
//        SecretKey secretKey = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
//        Cipher cipher = Cipher.getInstance(ALGORITHM);
//        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
//        byte[] encryptedBytes = cipher.doFinal(email.getBytes(StandardCharsets.UTF_8));
//        return Base64.getEncoder().encodeToString(encryptedBytes);
//    }
//
//    public static String decryptEmail(String encryptedEmail) throws Exception {
//        SecretKey secretKey = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
//        Cipher cipher = Cipher.getInstance(ALGORITHM);
//        cipher.init(Cipher.DECRYPT_MODE, secretKey);
//        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedEmail));
//        return new String(decryptedBytes, StandardCharsets.UTF_8);
//    }
}
