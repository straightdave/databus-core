package com.blueline.databus.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DBConfig {
    public String getDriverManager() {
        return driverManager;
    }

    @Value("${db.core.driverManager}")
    private String driverManager;

    public String getUrl() {
        return url;
    }

    @Value("${db.core.url}")
    private String url;

    public String getUsername() {
        return username;
    }

    @Value("${db.core.username}")
    private String username;

    public String getPassword() {
        return password;
    }

    @Value("${db.core.password}")
    private String password;
}
