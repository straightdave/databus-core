package com.blueline.databus.core.configtype;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DefaultConfig {

    @Value("${default.defaultTakes}")
    private String defaultTakes;

    @Value("${default.maxPOSTLength}")
    private int maxPOSTLength;

    public String getDefaultTakes() {
        return defaultTakes;
    }

    public int getMaxPOSTLength() {
        return maxPOSTLength;
    }
}
