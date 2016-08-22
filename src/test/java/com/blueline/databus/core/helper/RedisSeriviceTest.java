package com.blueline.databus.core.helper;

import static org.junit.Assert.*;

import com.blueline.databus.core.dao.AclCacheService;
import com.blueline.databus.core.dao.ApiRecordService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisSeriviceTest {

    @Autowired
    private AclCacheService aclCacheService;

    @Autowired
    private ApiRecordService apiRecordService;

    @Test
    public void can_record_api_call() {
        this.apiRecordService.flushDB();

        String apiPath = "/api/data/db1/table1";
        this.apiRecordService.recordAPICall(apiPath);
        assertEquals(1, this.apiRecordService.getAPICallCount(apiPath));

        this.apiRecordService.recordAPICall(apiPath);
        this.apiRecordService.recordAPICall(apiPath);
        this.apiRecordService.recordAPICall(apiPath);
        assertEquals(4, this.apiRecordService.getAPICallCount(apiPath));

        this.apiRecordService.flushDB();
    }
}
