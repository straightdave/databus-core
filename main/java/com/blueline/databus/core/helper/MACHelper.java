package com.blueline.databus.core.helper;

import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 通过一定的算法计算MAC值
 */
public class MACHelper {
    private static final Logger logger = Logger.getLogger(MACHelper.class);

    /**
     * 目前计算MAC的算法:
     * <pre>
     *     <code> MAC = toHexString(MD5.hash("{skey}#{payload}")) </code>
     * </pre>
     *
     * @param skey 客户端的secure key
     * @param payload 需要计算的信息实体
     * @return MAC字符
     */
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
