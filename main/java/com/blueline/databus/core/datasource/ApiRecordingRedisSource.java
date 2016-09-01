package com.blueline.databus.core.datasource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 使用Jedis资源池
 * 配置用于API调用记录的redis数据服务
 */
@Configuration
public class ApiRecordingRedisSource {

    @Value("${redis.host}")
    private String host;

    @Value("${redis.port}")
    private int port;

    @Value("${redis.db.recordApi}")
    private int db_recordApi;

    @Bean(name = "redis4Record")
    public JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory factory = new JedisConnectionFactory(new JedisPoolConfig());
        factory.setHostName(host);
        factory.setPort(port);
        factory.setDatabase(db_recordApi);
        return factory;
    }

    @Bean(name = "redisTemplate4Record")
    public StringRedisTemplate stringRedisTemplate(JedisConnectionFactory redis4Record){
        return new StringRedisTemplate(redis4Record);
    }
}
