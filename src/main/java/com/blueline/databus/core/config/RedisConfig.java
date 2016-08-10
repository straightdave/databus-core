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

    @Value("${redis.db.recordApi}")
    private int db_recordApi;

    @Value("${redis.db.accessCache}")
    private int db_accessCache;

    public int getDb_recordApi() {
        return db_recordApi;
    }

    public int getDb_accessCache() {
        return db_accessCache;
    }
}
