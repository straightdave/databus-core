package com.blueline.databus.core.helper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisHelperTest {

    @Autowired
    private RedisHelper redisHelper;

    @Before
    public void setUp() {
        // TODO: 风险。如果测试和生产使用同一个配置,测试可能会清除生产数据!
        // TODO: 测试使用测试的配置和环境!
        redisHelper.flushAll();
    }

    @Test
    public void can_record_api_call() {
        String apiPath = "/api/data/db1/table1";;
        redisHelper.recordAPICall(apiPath);
        assertEquals("1", redisHelper.getAPICallCount(apiPath));

        redisHelper.recordAPICall(apiPath);
        redisHelper.recordAPICall(apiPath);
        redisHelper.recordAPICall(apiPath);
        assertEquals("4", redisHelper.getAPICallCount("/api/data/db1/table1"));
    }
}
