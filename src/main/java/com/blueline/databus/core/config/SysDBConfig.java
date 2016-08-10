package com.blueline.databus.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * databus系统数据表的配置bean
 */
@Component
public class SysDBConfig {
    public String getDriverManager() {
        return driverManager;
    }

    @Value("${db.sys.driverManager}")
    private String driverManager;

    public String getUrl() {
        return url;
    }

    @Value("${db.sys.url}")
    private String url;

    public String getUsername() {
        return username;
    }

    @Value("${db.sys.username}")
    private String username;

    public String getPassword() {
        return password;
    }

    @Value("${db.sys.password}")
    private String password;
}
