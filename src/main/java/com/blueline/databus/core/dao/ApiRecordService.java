package com.blueline.databus.core.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ApiRecordService {

    @Autowired
    private StringRedisTemplate redisTemplate4Record;

    public void recordAPICall(String apiPath) {
        try {
            this.redisTemplate4Record.opsForValue().increment(apiPath, 1);
        }
        catch (Exception ex) {
            // eat it
            System.err.println("==> Recording Api Call: " + ex.getMessage());
        }
    }

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

    public void flushDB() {
        this.redisTemplate4Record.getConnectionFactory().getConnection().flushDb();
    }
}
