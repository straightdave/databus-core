package com.blueline.databus.core.datasource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class AclCacheRedisSource {

    @Value("${redis.host}")
    private String host;

    @Value("${redis.port}")
    private int port;

    @Value("${redis.db.accessCache}")
    private int db_accessCache;

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory factory = new JedisConnectionFactory(new JedisPoolConfig());
        factory.setHostName(host);
        factory.setPort(port);
        factory.setDatabase(db_accessCache);
        return factory;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(){
        return new StringRedisTemplate(jedisConnectionFactory());
    }
}
