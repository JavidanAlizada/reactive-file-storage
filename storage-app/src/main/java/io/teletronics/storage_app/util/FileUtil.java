package io.teletronics.storage_app.util;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileUtil {
    private static final String FILE_HASH_ALGORITHM = "SHA-256";

    public static String calculateFileHash(byte[] fileContent) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(FILE_HASH_ALGORITHM);
        byte[] hash = digest.digest(fileContent);
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
