package com.blueline.databus.core.filter;

import com.blueline.databus.core.helper.SysDBHelper;
import com.blueline.databus.core.helper.MACHelper;
import com.blueline.databus.core.bean.RestResult;
import com.blueline.databus.core.bean.ResultType;
import com.blueline.databus.core.config.AdminConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.http.HttpHeaders;
import static org.junit.Assert.*;

/**
 * 记录了对AuthenticationFilter的测试
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@IntegrationTest("server.port:8888")
public class AuthenticationFilterTest {

    private TestRestTemplate restTemplate = new TestRestTemplate();

    @Autowired
    private SysDBHelper sysDBHelper;

    @Autowired

    private AdminConfig adminConfig;

    @Test
    public void has_no_x_appkey() {
        // 测试filter,不需要给出真实的db和table名称
        String dummyApi = "http://localhost:8888/api/data/db1/table1?id=1";
        RestResult result = restTemplate.getForObject(dummyApi, RestResult.class);

        assertEquals(ResultType.FAIL, result.getResultType());
        assertEquals("header x-appkey missed", result.getMessage());
    }

    @Test
    public void has_appkey_but_no_mac() {
        String dummyApi = "http://localhost:8888/api/data/db1/table1?id=1";

        HttpHeaders headers = new HttpHeaders();
        headers.add("x-appkey", "SomeAppKey");

        ResponseEntity<RestResult> result = restTemplate.exchange(
                dummyApi, HttpMethod.GET, new HttpEntity<>(headers), RestResult.class);

        assertEquals(ResultType.FAIL, result.getBody().getResultType());
        assertEquals("header x-mac missed", result.getBody().getMessage());
    }

    @Test
    public void has_wrong_mac() {
        String dummyApi = "http://localhost:8888/api/data/db1/table1?id=1";

        HttpHeaders headers = new HttpHeaders();
        headers.add("x-appkey", adminConfig.getAppkey());
        headers.add("x-mac", "WHATEVER");

        ResponseEntity<RestResult> result = restTemplate.exchange(
                dummyApi, HttpMethod.GET, new HttpEntity<>(headers), RestResult.class);

        assertEquals(ResultType.FAIL, result.getBody().getResultType());
        assertTrue(result.getBody().getMessage().contains("MAC not match"));
    }

    @Test
    public void has_skey_admin_right() {
        String dummyApi = "http://localhost:8888/api/data/db1/table1?id=1";

        // 如果appkey是XYZ123,其skey也是
        String reqMac = MACHelper.calculateMAC(adminConfig.getSkey(), "/api/data/db1/table1?id=1");

        HttpHeaders headers = new HttpHeaders();
        headers.add("x-appkey", adminConfig.getAppkey());
        headers.add("x-mac", reqMac);

        ResponseEntity<RestResult> result = restTemplate.exchange(
                dummyApi, HttpMethod.GET, new HttpEntity<>(headers), RestResult.class);

        assertNotEquals(ResultType.FAIL, result.getBody().getResultType());
        assertFalse(result.getBody().getMessage().contains("MAC not match"));
    }

    @Test
    public void get_skey_from_db_right() {
        assertNotNull("DBHelper could be autowired", sysDBHelper);

        // 测试数据中有记录,其appkey味appkey1, skey为skey1

        String skey = sysDBHelper.getSKey("appkey1");

        String dummyApi = "http://localhost:8888/api/data/db1/table1?id=1";
        String reqMac = MACHelper.calculateMAC(skey, "/api/data/db1/table1?id=1");

        HttpHeaders headers = new HttpHeaders();
        headers.add("x-appkey", "appkey1");
        headers.add("x-mac", reqMac);

        ResponseEntity<RestResult> result = restTemplate.exchange(
                dummyApi, HttpMethod.GET, new HttpEntity<>(headers), RestResult.class);

        assertNotEquals(ResultType.FAIL, result.getBody().getResultType());
        assertFalse(result.getBody().getMessage().contains("MAC not match"));
    }
}
