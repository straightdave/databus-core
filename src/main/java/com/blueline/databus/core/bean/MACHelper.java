package com.blueline.databus.core.bean;

import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MACHelper {
    private static final Logger logger = Logger.getLogger(MACHelper.class);

    public static String calculateMAC(String skey, String payload) {
        String origin = skey + "#" + payload;
        String result = "";

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(origin.getBytes("UTF-8"));
            result = toHexString(digest);
        }
        catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            logger.error(ex.getMessage());
        }

        return result;
    }

    private static String toHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
