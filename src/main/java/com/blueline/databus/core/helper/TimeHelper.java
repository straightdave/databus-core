package com.blueline.databus.core.helper;

import org.springframework.util.StringUtils;

public class TimeHelper {
    /**
     * 检查输入的'HHmm'时间字符串是否在设定的'HHmmHHmm'范围内
     * @param nowDate 输入的当前时间点,格式'HHmm'
     * @param duration 设定的时间范围,格式'HHmmHHmm'
     * @return true/false
     */
    public static boolean isInDuration(String nowDate, String duration) {
        if (StringUtils.isEmpty(nowDate)  ||
            StringUtils.isEmpty(duration) ||
            nowDate.length() != 4         ||
            (duration.length() != 8 && duration.length() != 1) ||
            (duration.length() == 1 && !duration.equals("0"))) {
            return false;
        }

        if (duration.equals("0")) {
            return true;
        }

        String start_at = duration.substring(0, 4);
        String end_at = duration.substring(4);

        if (end_at.compareTo(start_at) < 0) {
            return false;
        }

        return (nowDate.compareTo(start_at) >= 0 && nowDate.compareTo(end_at) <= 0);
    }
}
