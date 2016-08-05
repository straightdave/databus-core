package com.blueline.databus.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DBConfig {
    public String getDriverManager() {
        return driverManager;
    }

    @Value("${db.driverManager}")
    private String driverManager;

    public String getUrl() {
        return url;
    }

    @Value("${db.url}")
    private String url;

    public String getUsername() {
        return username;
    }

    @Value("${db.username}")
    private String username;

    public String getPassword() {
        return password;
    }

    @Value("${db.password}")
    private String password;
}
