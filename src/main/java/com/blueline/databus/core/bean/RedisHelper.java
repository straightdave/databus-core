package com.blueline.databus.core.bean;

import com.blueline.databus.core.config.RedisConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisException;

@Component
public class RedisHelper {

    private static JedisPool jedisPool = null;
    private RedisConfig redisConfig;

    @Autowired
    private RedisHelper(RedisConfig config) {
        redisConfig = config;

        if (jedisPool == null) {
            jedisPool = new JedisPool(
                    new JedisPoolConfig(),
                    redisConfig.getHost(),
                    redisConfig.getPort());
        }
    }

    public void recordAPICall(String apiPath) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(Integer.valueOf(redisConfig.getDb()));
            jedis.incr(apiPath);
        }
        catch (JedisException jex) {
            System.err.print("RedisHelperError: " + jex.getMessage());
        }
    }

    public void insertAccessibities(String hashKey, String time) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(Integer.valueOf(redisConfig.getDb()));
            jedis.set(hashKey, time);
        }
        catch (JedisException jex) {
            System.err.print("RedisHelperError: " + jex.getMessage());
        }
    }

    public String getSKey(String appKey) {
        String result = null;
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(Integer.valueOf(redisConfig.getDb()));
            result = jedis.get(appKey);
        }
        catch (JedisException jex) {
            System.err.print("RedisHelperError: " + jex.getMessage());
        }
        return result;
    }
}