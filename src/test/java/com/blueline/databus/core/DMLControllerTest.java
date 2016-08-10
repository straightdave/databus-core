package com.blueline.databus.core;

import com.blueline.databus.core.bean.RestResult;
import com.blueline.databus.core.bean.ResultType;
import org.junit.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;

@RunWith(SpringRunner.class)
@WebAppConfiguration
public class DMLControllerTest {

    private TestRestTemplate template = new TestRestTemplate();
    private final String baseUri = "http://localhost:8888";

//    @Test
//    public void createTable() {
//        Object obj = "{\"name\":\"t100\",\"fields\":[{\"col_name\":\"id\",\"data_type\":\"int\",\"is_null\":\"true\",\"auto_increment\":\"true\",\"is_pk\":\"true\",\"length\":\"6\"},{\"col_name\":\"name\",\"data_type\":\"varchar\",\"length\":\"50\"},{\"col_name\":\"age\",\"data_type\":\"int\"}],\"comment\":\"注释\",\"account_name\":\"Administrator\"}";
//        String res = template.postForObject(baseUri + "/db/test", obj, String.class);
//
//    }

    @Test
    public void get_data_simple() {
        RestResult result = template.getForObject(baseUri + "/data/db1/table1?id=1", RestResult.class);
        assertEquals(ResultType.FAIL, result.getResultType());
    }
    
}
