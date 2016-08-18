package com.blueline.databus.core.configtype;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SysDBConfig {

    @Value("${db.sys.driverManager}")
    private String driverManager;

    @Value("${db.sys.url}")
    private String url;

    @Value("${db.sys.username}")
    private String username;

    @Value("${db.sys.password}")
    private String password;

    public String getDriverManager() {
        return driverManager;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
