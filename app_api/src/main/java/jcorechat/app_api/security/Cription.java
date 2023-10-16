package jcorechat.app_api.security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.Base64;

public class Cription {

    private SecretKey Encription_Key;

    public Cription() {
        Encription_Key = generateSecretKey();
    }

    public boolean canRun() {
        return Encription_Key != null;
    }



    private SecretKey generateSecretKey() {
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
        } catch (Exception e) {
            e.printStackTrace();
            return null; }
        keyGenerator.init(128); // You can use 128, 192, or 256 bits key size
        return keyGenerator.generateKey();
    }

    public String encrypt(String input) {
        if (null == Encription_Key) { return null; }

        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, Encription_Key);
            return Base64.getEncoder().encodeToString(cipher.doFinal(input.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
            return null; }
    }

    public String decrypt(String cipherText)  {
        if (null == Encription_Key ) { return null; }

        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            // AES/CBC/PKCS5Padding
            cipher.init(Cipher.DECRYPT_MODE, Encription_Key);
            return new String(cipher.doFinal(Base64.getDecoder().decode(cipherText)));
        } catch (Exception e) {
            e.printStackTrace();
            return null; }
    }
}
