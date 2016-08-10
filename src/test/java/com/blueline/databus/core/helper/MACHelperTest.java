package com.blueline.databus.core.helper;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.util.StringUtils;

public class MACHelperTest {

    @Test
    public void gen_mac() {
        String result = MACHelper.calculateMAC("skey1", "payload1");
        assertFalse(StringUtils.isEmpty(result));
        System.out.println(result);
        assertEquals(32, result.length());
    }

    @Test
    public void gen_with_bad_param() {
        String result = MACHelper.calculateMAC("", "");
        assertFalse(StringUtils.isEmpty(result));
        System.out.println(result);
        assertEquals(32, result.length());
    }

    @Test
    public void gen_with_long_param() {
        String result = MACHelper.calculateMAC("1231231123123123123123123234234234234234234234", "asdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasdfasd");
        assertFalse(StringUtils.isEmpty(result));
        System.out.println(result);
        assertEquals(32, result.length());
    }
}