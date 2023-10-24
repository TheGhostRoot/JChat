package jcorechat.app_api.security;

import jcorechat.app_api.API;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;


@Service
public class Cription {

    private SecretKey GlobalEncription_Key = null;

    public Cription() {
        // GlobalEncription_Key = generateSecretKey();
        GlobalEncription_Key = getKeyFromString("P918nfQtYhbUzJVbmSQfZw==");
        API.logger.info("Global Encryption Key: "+Base64.getEncoder().encodeToString(GlobalEncription_Key.getEncoded()));
    }

    public String generateKey() {
        return Base64.getEncoder().encodeToString(generateSecretKey().getEncoded());
    }

    public String generateUserKey() {
        String key = generateKey();
        while (API.encryption_user_keys.containsValue(key)) {
            key = generateKey();
        }
        return key;
    }

    public SecretKey getKeyFromString(String key) {
        byte[] decodedKey = Base64.getDecoder().decode(key);
        try {
            return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        } catch (Exception e) { return null; }
    }

    private SecretKey generateSecretKey() {
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
        } catch (Exception e) { return null; }
        keyGenerator.init(128); // You can use 128, 192, or 256 bits key size
        return keyGenerator.generateKey();
    }

    public String GlobalEncrypt(String input) {
        if (null == GlobalEncription_Key) { return null; }

        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, GlobalEncription_Key);
            return Base64.getEncoder().encodeToString(cipher.doFinal(input.getBytes()));
        } catch (Exception e) { return null; }
    }

    public String UserEncrypt(String input, String key) {
        SecretKey SecKey = getKeyFromString(key);
        if (null == SecKey) { return null; }

        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, SecKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(input.getBytes()));
        } catch (Exception e) { return null; }
    }

    public String GlobalDecrypt(String cipherText)  {
        if (null == GlobalEncription_Key ) { return null; }

        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            // AES/CBC/PKCS5Padding
            cipher.init(Cipher.DECRYPT_MODE, GlobalEncription_Key);
            return new String(cipher.doFinal(Base64.getDecoder().decode(cipherText)));
        } catch (Exception e) { return null; }
    }

    public String UserDecrypt(String cipherText, String key)  {
        SecretKey SecKey = getKeyFromString(key);
        if (null == SecKey) { return null; }

        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            // AES/CBC/PKCS5Padding
            cipher.init(Cipher.DECRYPT_MODE, SecKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(cipherText)));
        } catch (Exception e) { return null; }
    }
}
