package com.blueline.databus.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DefaultConfig {
    public String getDefaultTakes() {
        return defaultTakes;
    }

    @Value("${default.defaultTakes}")
    private String defaultTakes;
}
