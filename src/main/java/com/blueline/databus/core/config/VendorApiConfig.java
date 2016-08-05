package com.blueline.databus.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VendorApiConfig {
    public String getAccessCheck() {
        return accessCheck;
    }

    @Value("${vendorApi.accessCheck}")
    private String accessCheck;
}
