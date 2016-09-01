package com.blueline.databus.core.helper;

import org.junit.Test;
import static org.junit.Assert.*;

public class TimeHelperTest {

    @Test
    public void can_compare_right() {
        assertTrue(TimeHelper.isInDuration("0123", "0"));
        assertFalse(TimeHelper.isInDuration("0001", "11112333"));
        assertFalse(TimeHelper.isInDuration("2222", "00001111"));
        assertTrue(TimeHelper.isInDuration("2359", "0"));
        assertTrue(TimeHelper.isInDuration("2359", "11112359"));
    }

    @Test
    public void can_handle_bad_param() {
        assertFalse(TimeHelper.isInDuration("12345", "0"));
        assertFalse(TimeHelper.isInDuration("123", "0"));
        assertFalse(TimeHelper.isInDuration("1234", "1"));
        assertFalse(TimeHelper.isInDuration("1234", "111"));
        assertFalse(TimeHelper.isInDuration("1234", "56781234"));
    }
}
