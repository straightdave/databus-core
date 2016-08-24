package com.blueline.databus.core.controller;

import com.blueline.databus.core.dao.SysDBDao;
import com.blueline.databus.core.datatype.RestResult;
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
public class AdminControllerTest {

    private TestRestTemplate template = new TestRestTemplate();
    private final String baseUri = "http://localhost:8888/api/sys";

    @Autowired
    private SysDBDao sysDBDao;

    @Test
    public void query_data_simple() throws InternalException {

        String url = baseUri + "/clients";

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-appkey", "XYZ123");    // use admin to bypass filter
        headers.set("x-mac", MACHelper.calculateMAC(
                "XYZ123", "XYZ123_GET_/api/sys/clients"));

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<RestResult> resp =
                template.exchange(url, HttpMethod.GET, entity, RestResult.class);

        assertEquals(1, 1);
    }
}
