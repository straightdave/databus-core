package com.blueline.databus.core.filter;

import com.blueline.databus.core.dao.SysDBDao;
import com.blueline.databus.core.exception.InternalException;
import com.blueline.databus.core.helper.MACHelper;
import com.blueline.databus.core.datatype.RestResult;
import com.blueline.databus.core.datatype.ResultType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private SysDBDao sysDBDao;

    @Value("${admin.appkey}")
    private String adminAppKey;

    @Value("${admin.skey}")
    private String adminSKey;

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
        String dummyApi = "http://localhost:8888/api/data/databus_core/table1?id=1";

        HttpHeaders headers = new HttpHeaders();
        headers.add("x-appkey", adminAppKey); // can pass thru filter
        headers.add("x-mac", "WHATEVER");

        System.out.println("==> test x-appkey=" + adminAppKey);

        ResponseEntity<RestResult> result = restTemplate.exchange(
                dummyApi, HttpMethod.GET, new HttpEntity<>(headers), RestResult.class);

        assertEquals(ResultType.FAIL, result.getBody().getResultType());
        assertTrue(result.getBody().getMessage().contains("MAC not match"));
    }

    @Test
    public void has_skey_admin_right() {
        String dummyApi = "http://localhost:8888/api/data/db1/table1?id=1";

        // 如果appkey是XYZ123,其skey也是
        String reqMac = MACHelper.calculateMAC(adminSKey, "XYZ123_GET_/api/data/db1/table1?id=1");

        HttpHeaders headers = new HttpHeaders();
        headers.add("x-appkey", adminAppKey);
        headers.add("x-mac", reqMac);

        ResponseEntity<RestResult> result = restTemplate.exchange(
                dummyApi, HttpMethod.GET, new HttpEntity<>(headers), RestResult.class);

        assertNotEquals(ResultType.FAIL, result.getBody().getResultType());
        assertFalse(result.getBody().getMessage().contains("MAC not match"));
    }

    @Test
    public void get_skey_from_db_right() throws InternalException {
        assertNotNull("SysDBDao could be autowired", sysDBDao);

        // clean and create test client
        sysDBDao.deleteClient("test_client");
        sysDBDao.createClient("{\"name\":\"test_client\"}");

        String appkey = sysDBDao.getClientByName("test_client").getAppKey();
        assertNotNull(appkey);
        assertNotEquals("", appkey);

        String skey = sysDBDao.getClientByName("test_client").getSKey();
        assertNotNull(skey);
        assertNotEquals("", skey);

        String dummyApi = "http://localhost:8888/api/sys/client/test_client";
        String reqMac = MACHelper.calculateMAC(skey, appkey + "_GET_/api/sys/client/test_client");

        HttpHeaders headers = new HttpHeaders();
        headers.add("x-appkey", appkey);
        headers.add("x-mac", reqMac);

        ResponseEntity<RestResult> resp = restTemplate.exchange(
                dummyApi, HttpMethod.GET, new HttpEntity<>(headers), RestResult.class);

        System.out.println(resp.getBody());

        assertEquals(ResultType.OK, resp.getBody().getResultType());

        // clean
        sysDBDao.deleteClient("test_client");
    }
}
