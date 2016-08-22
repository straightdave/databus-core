package com.blueline.databus.core.helper;

import java.security.*;

public class RandomStringHelper {
    private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static SecureRandom rnd = new SecureRandom();

    /**
     * 根据基础字符集(AB)
     * 生成任意长度的随机字符串
     * @param len 随机字符串长度
     * @return 随机字符串
     */
    public static String getRandomString(int len) {
        if (len <= 0) {
            len = 8; // default len 8
        }

        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        }
        return sb.toString();
    }

    /**
     * 根据输入字符串生成hex hash字符串
     * @param inputStr 输入字符串
     * @return 输出字符串(hex hash)
     */
    public static String hashKey(final String inputStr) {
        // append str length for less collision of hashCode()
        int hashValue = String.format("%s%d", inputStr, inputStr.length()).hashCode();
        return Integer.toHexString(hashValue);
    }
}
