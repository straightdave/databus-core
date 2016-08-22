package com.blueline.databus.core.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * 提供API访问统计功能的缓存服务
 */
@Repository
public class ApiRecordService {

    @Autowired
    private StringRedisTemplate redisTemplate4Record;

    /**
     * 给某个api访问计数加1
     * @param apiPath api路径(作为键值)
     */
    public void recordAPICall(String apiPath) {
        try {
            this.redisTemplate4Record.opsForValue().increment(apiPath, 1);
        }
        catch (Exception ex) {
            // eat it
            System.err.println("==> Recording Api Call: " + ex.getMessage());
        }
    }

    /**
     * 读取某api的访问计数
     * @param apiPath api路径(键值)
     * @return 访问计数
     */
    public int getAPICallCount(String apiPath) {
        int result;
        try {
            result = Integer.valueOf(this.redisTemplate4Record.opsForValue().get(apiPath));
        }
        catch (Exception ex) {
            // eat it
            System.err.println("==> Get Api Call: " + ex.getMessage());
            result = -1;
        }
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
