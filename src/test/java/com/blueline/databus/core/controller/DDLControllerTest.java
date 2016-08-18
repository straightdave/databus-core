package com.blueline.databus.core.controller;

import com.blueline.databus.core.dao.CoreDBDao;
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
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@IntegrationTest("server.port:8888")
public class DDLControllerTest {

    @Autowired
    private CoreDBDao coreDBDao;

    @Test
    public void drop_non_exist_table() {

        TestRestTemplate template = new TestRestTemplate();
        String url = "http://localhost:8888/api/db/core/tb1";

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-appkey", "XYZ123");
        headers.set("x-mac", MACHelper.calculateMAC("XYZ123", "/api/db/core/tb1"));

        HttpEntity<String> entity = new HttpEntity<>("delete", headers);
        ResponseEntity<RestResult> resp =
                template.exchange(url, HttpMethod.DELETE, entity, RestResult.class);

        assertEquals(HttpStatus.OK,resp.getStatusCode());
        assertEquals(ResultType.ERROR, resp.getBody().getResultType());

        System.out.println(resp.getBody().getMessage());
    }

    @Test
    public void drop_existing_table() {
        try {
            String jsonBody = "[{\"name\":\"username\",\"type\":\"varchar(255)\"}]";
            coreDBDao.createTable("databus_core", "testtable1", jsonBody);
        }
        catch (InternalException ex) {
            System.err.println("test: drop existing table: create table failed: " + ex.getMessage());
        }

        TestRestTemplate template = new TestRestTemplate();
        String url = "http://localhost:8888/api/db/databus_core/testtable1";

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-appkey", "XYZ123");
        headers.set("x-mac", MACHelper.calculateMAC("XYZ123", "XYZ123_DELETE_/api/db/databus_core/testtable1"));

        HttpEntity<String> entity = new HttpEntity<>("delete", headers);
        ResponseEntity<RestResult> resp =
                template.exchange(url, HttpMethod.DELETE, entity, RestResult.class);

        System.out.println(resp.getBody().getMessage());

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals(ResultType.OK, resp.getBody().getResultType());


    }

    @Test
    public void can_create_table() {
        TestRestTemplate template = new TestRestTemplate();
        String url = "http://localhost:8888/api/db/databus_core/tb1";

        String body = "[{\"name\":\"name\",\"type\":\"varchar(50)\",\"nullable\":\"false\"}]";

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-appkey", "XYZ123");
        headers.set("Content-Type", "application/json");
        headers.set("x-mac", MACHelper.calculateMAC("XYZ123", "XYZ123_POST_/api/db/databus_core/tb1"));

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<RestResult> resp =
                template.exchange(url, HttpMethod.POST, entity, RestResult.class);

//        assertEquals(HttpStatus.OK, resp.getStatusCode());
//        assertEquals(ResultType.OK, resp.getBody().getResultType());

        System.out.println(resp.getBody().getMessage());
    }
}
