package com.blueline.databus.core.configtype;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AdminConfig {

    @Value("${admin.appkey}")
    private String appkey;

    @Value("${admin.skey}")
    private String skey;

    public String getAppkey() {
        return appkey;
    }

    public String getSkey() {
        return skey;
    }
}
