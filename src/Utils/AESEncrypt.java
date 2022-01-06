package Utils;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Arrays;

public class AESEncrypt {
    private static final byte[] tlsKey = "UkXp2s5v8y/B?E(H+MbQeThVmYq3t6w9".getBytes();

    public static String encrypt(String data) {
        try {
            Key key = new SecretKeySpec(tlsKey, "AES");
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.ENCRYPT_MODE, key);
            return Arrays.toString(c.doFinal(data.getBytes()));
        } catch (Exception ignored) {}

        return data;
    }

    public static String decrypt(String encryptedData) {
        try {
            Key key = new SecretKeySpec(tlsKey, "AES");
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.DECRYPT_MODE, key);
            return new String(c.doFinal(encryptedData.getBytes()));
        } catch (Exception ignored) {}

        return encryptedData;
    }
}