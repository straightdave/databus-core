package com.blueline.databus.core.filter;

import com.blueline.databus.core.bean.ResultType;
import com.blueline.databus.core.helper.MACHelper;
import com.blueline.databus.core.bean.RestResult;
import com.blueline.databus.core.config.AdminConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@IntegrationTest("server.port:8888")
public class AuthorityFilterTest {

    private TestRestTemplate restTemplate = new TestRestTemplate();

    @Autowired
    private AdminConfig adminConfig;

    @Test
    public void non_admin_has_no_access() {
        String dummyApi = "http://localhost:8888/api/data/db1/table1?id=1";

        String reqMac = MACHelper.calculateMAC("skey1", "/api/data/db1/table1?id=1");

        HttpHeaders headers = new HttpHeaders();
        headers.add("x-appkey", "appkey1");
        headers.add("x-mac", reqMac);

        ResponseEntity<RestResult> result = restTemplate.exchange(
                dummyApi, HttpMethod.GET, new HttpEntity<>(headers), RestResult.class);

        assertEquals(ResultType.FAIL, result.getBody().getResultType());
        assertEquals("No Access", result.getBody().getMessage());
    }
}
