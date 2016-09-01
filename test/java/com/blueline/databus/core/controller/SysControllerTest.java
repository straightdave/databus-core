package com.blueline.databus.core.controller;

import com.blueline.databus.core.dao.SysDBDao;
import com.blueline.databus.core.datatype.RestResult;
import com.blueline.databus.core.datatype.ResultType;
import com.blueline.databus.core.exception.InternalException;
import com.blueline.databus.core.helper.MACHelper;
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

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@IntegrationTest("server.port:8888")
public class SysControllerTest {
    private TestRestTemplate template = new TestRestTemplate();
    private final String baseUri = "http://localhost:8888/api/sys";

    @Autowired
    private SysDBDao sysDBDao;

    @Test
    public void get_all_clients() throws InternalException {
        String url = baseUri + "/clients";

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-appkey", "XYZ123");    // use admin to bypass filter
        headers.set("x-mac", MACHelper.calculateMAC(
                "XYZ123", "XYZ123_GET_/api/sys/clients"));

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<RestResult> resp =
                template.exchange(url, HttpMethod.GET, entity, RestResult.class);

        System.out.println(resp.getBody());
        assertEquals(ResultType.OK, resp.getBody().getResultType());
    }

    @Test
    public void get_client_by_name() {
        String url = baseUri + "/client/client1";

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-appkey", "XYZ123");
        headers.set("x-mac", MACHelper.calculateMAC(
                "XYZ123", "XYZ123_GET_/api/sys/client/client1"));

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<RestResult> resp =
                template.exchange(url, HttpMethod.GET, entity, RestResult.class);

        System.out.println(resp.getBody());
        assertEquals(ResultType.OK, resp.getBody().getResultType());
    }

    @Test
    public void get_acl_for_client() {
        String url = baseUri + "/client/client1/acl";

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-appkey", "XYZ123");
        headers.set("x-mac", MACHelper.calculateMAC(
                "XYZ123", "XYZ123_GET_/api/sys/client/client1/acl"));

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<RestResult> resp =
                template.exchange(url, HttpMethod.GET, entity, RestResult.class);

        System.out.println(resp.getBody());
        assertEquals(ResultType.OK, resp.getBody().getResultType());
    }

    @Test
    public void dump_acl_cache() {
        String url = baseUri + "/acl_cache";

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-appkey", "XYZ123");
        headers.set("x-mac", MACHelper.calculateMAC(
                "XYZ123", "XYZ123_GET_/api/sys/acl_cache"));

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<RestResult> resp =
                template.exchange(url, HttpMethod.GET, entity, RestResult.class);

        System.out.println(resp.getBody());
        assertEquals(ResultType.OK, resp.getBody().getResultType());
    }

    @Test
    public void dump_call_record() {
        String url = baseUri + "/call_records";

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-appkey", "XYZ123");
        headers.set("x-mac", MACHelper.calculateMAC(
                "XYZ123", "XYZ123_GET_/api/sys/call_records"));

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<RestResult> resp =
                template.exchange(url, HttpMethod.GET, entity, RestResult.class);

        System.out.println(resp.getBody());
        assertEquals(ResultType.OK, resp.getBody().getResultType());
    }
}
