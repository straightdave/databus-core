package com.blueline.databus.core.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 提供API访问统计功能的缓存服务
 */
@Repository
public class ApiRecordService {

    @Autowired
    private StringRedisTemplate redisTemplate4Record;

    /**
     * 给某个api访问计数加1
     * @param apiKey HTTP方法 + api路径(作为键值),如"GET /xxx/xxx"
     */
    public void recordAPICall(String apiKey) {
        try {
            System.out.println("going to record " + apiKey);
            this.redisTemplate4Record.opsForValue().increment(apiKey, 1);
        }
        catch (Exception ex) {
            // eat it
            System.err.println("==> Recording Api Call: " + ex.getMessage());
        }
    }

    /**
     * 读取某api的访问计数
     * @param apiKey api访问键值
     * @return 访问计数
     */
    public int getAPICallCount(String apiKey) {
        try {
            return Integer.valueOf(this.redisTemplate4Record.opsForValue().get(apiKey));
        }
        catch (Exception ex) {
            // eat it
            System.err.println("==> Get Api Call: " + ex.getMessage());
            return -1;
        }
    }

    /**
     * 返回目前缓存中所有acl信息
     * @return AclInfo实例列表
     */
    public Map<String, Integer> dumpAllCallRecord() {
        Map<String, Integer> result = new HashMap<>();
        this.redisTemplate4Record.keys("*").forEach(key -> {
            result.put(key, Integer.valueOf(this.redisTemplate4Record.opsForValue().get(key)));
        });
        return result;
    }

    /**
     * 清除本缓存所有数据
     * 主要用于测试目的
     */
    public void flushDB() {
        this.redisTemplate4Record.getConnectionFactory().getConnection().flushDb();
    }
}
