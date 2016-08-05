package com.blueline.databus.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AdminConfig {
    public String getAppkey() {
        return appkey;
    }

    @Value("${admin.appkey}")
    private String appkey;

    public String getSkey() {
        return skey;
    }

    @Value("${admin.skey}")
    private String skey;
}
