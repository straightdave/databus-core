package com.blueline.databus.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RedisConfig {
    public String getHost() {
        return host;
    }

    @Value("${redis.host}")
    private String host;

    public int getPort() {
        return port;
    }

    @Value("${redis.port}")
    private int port;

    public String getDb() {
        return db;
    }

    @Value("${redis.db}")
    private String db;
}
