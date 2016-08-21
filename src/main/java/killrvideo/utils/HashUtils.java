package killrvideo.utils;

import static org.apache.commons.lang3.StringUtils.isBlank;

import org.apache.commons.codec.digest.DigestUtils;

public class HashUtils {

    public static String hashPassword(String password) {
        return new String(DigestUtils.getSha512Digest().digest(password.getBytes()));
    }

    public static boolean isPasswordValid(String realPassword, String hash) {
        if (isBlank(realPassword) || isBlank(hash)) {
            return false;
        }

        return hashPassword(realPassword.trim()).compareTo(hash) == 0;
    }
}
