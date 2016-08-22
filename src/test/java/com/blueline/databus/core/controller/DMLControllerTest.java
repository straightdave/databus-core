package com.blueline.databus.core.controller;

import com.blueline.databus.core.dao.CoreDBDao;
import com.blueline.databus.core.datatype.RestResult;
import com.blueline.databus.core.datatype.ResultType;
import com.blueline.databus.core.exception.InternalException;
import com.blueline.databus.core.helper.MACHelper;
import org.junit.Test;
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
import org.junit.runner.RunWith;

@RunWith(SpringRunner.class)
@SpringBootTest
@IntegrationTest("server.port:8888")
public class DMLControllerTest {

    private TestRestTemplate template = new TestRestTemplate();
    private final static String baseUri = "http://localhost:8888/api";

    @Autowired
    private CoreDBDao coreDBDao;

    @Test
    public void query_data_simple() throws InternalException {
        String jsonBody = "[{\"name\":\"name\", \"type\":\"varchar(20)\"}]";
        coreDBDao.createTableIfNotExist("databus_core", "table1", jsonBody);

        System.out.println("==> test table created");

        jsonBody = "[{\"name\":\"dave\"}, {\"name\":\"fuck\"}]";
        int count = coreDBDao.insertData("databus_core", "table1", jsonBody);

        System.out.println("==> inserted data row = " + count);

        String url = baseUri + "/data/databus_core/table1";
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-appkey", "XYZ123");
        headers.set("x-mac", MACHelper.calculateMAC("XYZ123", "XYZ123_GET_/api/data/databus_core/table1"));

        HttpEntity<String> entity = new HttpEntity<>("get", headers);
        ResponseEntity<RestResult> resp =
                template.exchange(url, HttpMethod.GET, entity, RestResult.class);

        System.out.println("==> response: " + resp.getBody());

        assertEquals(ResultType.OK, resp.getBody().getResultType());

        coreDBDao.dropTableIfExist("databus_core", "table1");
        System.out.println("test table dropped");
    }

    @Test
    public void insert_simple_data() throws InternalException {
        String jsonBody = "[{\"name\":\"name\", \"type\":\"varchar(20)\", \"nullable\":\"false\"}," +
                "{\"name\":\"age\", \"type\":\"smallint\", \"nullable\":\"true\"}]";
        coreDBDao.createTableIfNotExist("databus_core", "table1", jsonBody);

        System.out.println("==> test table created");

        String url = baseUri + "/data/databus_core/table1";
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-appkey", "XYZ123");
        headers.set("x-mac", MACHelper.calculateMAC("XYZ123", "XYZ123_POST_/api/data/databus_core/table1"));

        String body = "[{\"name\":\"dave\",\"age\": 20}]";
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<RestResult> resp =
                template.exchange(url, HttpMethod.POST, entity, RestResult.class);

        System.out.println("==> response: " + resp.getBody());

        assertEquals(ResultType.OK, resp.getBody().getResultType());

        coreDBDao.dropTableIfExist("databus_core", "table1");
        System.out.println("==> test table dropped");
    }

    @Test
    public void update_simple() throws InternalException {
        String jsonBody = "[{\"name\":\"name\", \"type\":\"varchar(20)\"}]";
        coreDBDao.createTableIfNotExist("databus_core", "table1", jsonBody);

        System.out.println("==> test table created");

        jsonBody = "[{\"name\":\"dave\"}, {\"name\":\"fuck\"}]";
        int count = coreDBDao.insertData("databus_core", "table1", jsonBody);

        System.out.println("==> inserted data row = " + count);

        String url = baseUri + "/data/databus_core/table1/name/dave";
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-appkey", "XYZ123");
        headers.set("x-mac", MACHelper.calculateMAC("XYZ123", "XYZ123_PUT_/api/data/databus_core/table1/name/dave"));

        String body = "[{\"name\":\"dave very 666\"}]";
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<RestResult> resp =
                template.exchange(url, HttpMethod.PUT, entity, RestResult.class);

        System.out.println("==> response: " + resp.getBody());

        assertEquals(ResultType.OK, resp.getBody().getResultType());

        coreDBDao.dropTableIfExist("databus_core", "table1");
        System.out.println("test table dropped");
    }

    @Test
    public void update_not_exist_col() throws InternalException {
        String jsonBody = "[{\"name\":\"name\", \"type\":\"varchar(20)\"}]";
        coreDBDao.createTableIfNotExist("databus_core", "table1", jsonBody);

        System.out.println("==> test table created");

        jsonBody = "[{\"name\":\"dave\"}, {\"name\":\"fuck\"}]";
        int count = coreDBDao.insertData("databus_core", "table1", jsonBody);

        System.out.println("==> inserted data row = " + count);

        String url = baseUri + "/data/databus_core/table1/name/dave";
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-appkey", "XYZ123");
        headers.set("x-mac", MACHelper.calculateMAC("XYZ123", "XYZ123_PUT_/api/data/databus_core/table1/name/dave"));

        String body = "[{\"age\":\"666\"}]";
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<RestResult> resp =
                template.exchange(url, HttpMethod.PUT, entity, RestResult.class);

        System.out.println("==> response: " + resp.getBody());

        assertEquals(ResultType.ERROR, resp.getBody().getResultType());

        coreDBDao.dropTableIfExist("databus_core", "table1");
        System.out.println("test table dropped");
    }

    @Test
    public void update_no_row_to_update() throws InternalException {
        String jsonBody = "[{\"name\":\"name\", \"type\":\"varchar(20)\"}]";
        coreDBDao.createTableIfNotExist("databus_core", "table1", jsonBody);

        System.out.println("==> test table created");

        jsonBody = "[{\"name\":\"dave\"}, {\"name\":\"fuck\"}]";
        int count = coreDBDao.insertData("databus_core", "table1", jsonBody);

        System.out.println("==> inserted data row = " + count);

        String url = baseUri + "/data/databus_core/table1/name/nobody";
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-appkey", "XYZ123");
        headers.set("x-mac", MACHelper.calculateMAC("XYZ123", "XYZ123_PUT_/api/data/databus_core/table1/name/nobody"));

        String body = "[{\"name\":\"dave very 666\"}]";
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<RestResult> resp =
                template.exchange(url, HttpMethod.PUT, entity, RestResult.class);

        System.out.println("==> response: " + resp.getBody());

        assertEquals(ResultType.FAIL, resp.getBody().getResultType());

        coreDBDao.dropTableIfExist("databus_core", "table1");
        System.out.println("test table dropped");
    }
    
}
