package com.blueline.databus.core.controller;

import com.blueline.databus.core.dao.CoreDBDao;
import com.blueline.databus.core.datatype.RestResult;
import com.blueline.databus.core.datatype.ResultType;
import com.blueline.databus.core.exception.InternalException;
import com.blueline.databus.core.helper.MACHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.IOException;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@IntegrationTest("server.port:8888")
public class DMLControllerTest {

    private TestRestTemplate template = new TestRestTemplate();
    private final static String baseUri = "http://localhost:8888/api";

    @Autowired
    private CoreDBDao coreDBDao;

    @Test
    public void query_data_no_param() throws InternalException, IOException {
        coreDBDao.dropTableIfExist("databus_core", "table1");

        ArrayList<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> col1 = new HashMap<>();
        col1.put("name", "name");
        col1.put("data_type", "varchar");
        list.add(col1);
        coreDBDao.createTableIfNotExist("databus_core", "table1", list);
        System.out.println("==> test table created");

        String jsonBody = "[{\"name\":\"dave\"}, {\"name\":\"fuck\"}]";
        int count = coreDBDao.insertData("databus_core", "table1", jsonBody);
        System.out.println("==> inserted sample data row = " + count);

        String url = baseUri + "/data/databus_core/table1";

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-appkey", "XYZ123");    // use admin to bypass filter
        headers.set("x-mac", MACHelper.calculateMAC(
                "XYZ123", "XYZ123_GET_/api/data/databus_core/table1"));

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<RestResult> resp =
                template.exchange(url, HttpMethod.GET, entity, RestResult.class);

        System.out.println("==> query response: " + resp.getBody());

        assertEquals(ResultType.OK, resp.getBody().getResultType());

        String data = resp.getBody().getMessage();
        List<Map<String,String>> result = new ObjectMapper().readValue(data,List.class);
        assertEquals(2, result.size());

        coreDBDao.dropTableIfExist("databus_core", "table1");
        System.out.println("==> test table dropped");
    }

    @Test
    public void query_no_data() throws InternalException, IOException {
        // clean table
        coreDBDao.dropTableIfExist("databus_core", "table1");

        // create table
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> col1 = new HashMap<>();
        col1.put("name", "name");
        col1.put("data_type", "varchar");
        list.add(col1);
        coreDBDao.createTableIfNotExist("databus_core", "table1", list);
        System.out.println("==> test table created");

        // not insert any data

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-appkey", "XYZ123");    // use admin to bypass filter
        headers.set("x-mac", MACHelper.calculateMAC(
                "XYZ123", "XYZ123_GET_/api/data/databus_core/table1"));

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<RestResult> resp =
                template.exchange(
                        baseUri + "/data/databus_core/table1",
                        HttpMethod.GET,
                        entity,
                        RestResult.class);

        System.out.println("==> query response: " + resp.getBody());
        assertEquals(ResultType.OK, resp.getBody().getResultType());

        // clean table
        coreDBDao.dropTableIfExist("databus_core", "table1");
        System.out.println("==> test table dropped");
    }

    @Test
    public void query_data_simple_equal() throws InternalException, IOException {
        coreDBDao.dropTableIfExist("databus_core", "table1");

        ArrayList<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> col1 = new HashMap<>();
        col1.put("name", "name");
        col1.put("data_type", "varchar");

        Map<String,Object> col2 = new HashMap<>();
        col2.put("name", "age");
        col2.put("data_type", "smallint unsigned");

        Map<String,Object> col3 = new HashMap<>();
        col3.put("name", "born_at");
        col3.put("type", "datetime");

        list.add(col1);
        list.add(col2);
        list.add(col3);

        coreDBDao.createTableIfNotExist("databus_core", "table1", list);
        System.out.println("==> test table created");

        String jsonBody =
                "[{\"name\":\"dave\",\"age\":\"19\",\"born_at\":\"1995/08/11\"}, " +
                "{\"name\":\"dave2\",\"age\":\"20\",\"born_at\":\"1995/08/11\"}, " +
                "{\"name\":\"dave3\",\"age\":\"21\",\"born_at\":\"1995/08/11\"}, " +
                "{\"name\":\"dave4\",\"age\":\"23\",\"born_at\":\"1995/08/11\"}, " +
                "{\"name\":\"fuck\",\"age\":\"16\",\"born_at\":\"2000/04/01\"}]";
        int count = coreDBDao.insertData("databus_core", "table1", jsonBody);
        System.out.println("==> inserted sample data row = " + count);

        String url = baseUri + "/data/databus_core/table1?name=dave";

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-appkey", "XYZ123");    // use admin to bypass filter
        headers.set("x-mac", MACHelper.calculateMAC(
                "XYZ123", "XYZ123_GET_/api/data/databus_core/table1?name=dave"));

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<RestResult> resp =
                template.exchange(url, HttpMethod.GET, entity, RestResult.class);

        System.out.println("==> query response: " + resp.getBody());

        assertEquals(ResultType.OK, resp.getBody().getResultType());

        ObjectMapper om = new ObjectMapper();
        List<Map<String, Object>> objects = om.readValue(resp.getBody().getMessage(), List.class);

        assertEquals(1, objects.size());
        assertEquals("dave", objects.get(0).get("name"));
        System.out.println("==> json resolved ok and query result check ok: ?name=dave");

        coreDBDao.dropTableIfExist("databus_core", "table1");
        System.out.println("==> test table dropped");
    }

    @Test
    public void query_data_start_end() throws InternalException, IOException {
        coreDBDao.dropTableIfExist("databus_core", "table1");

        ArrayList<Map<String,Object>> list = new ArrayList<>();
        Map<String,Object> col1 = new HashMap<>();
        col1.put("name", "name");
        col1.put("data_type", "varchar");

        Map<String,Object> col2 = new HashMap<>();
        col2.put("name", "age");
        col2.put("data_type", "smallint unsigned");

        Map<String,Object> col3 = new HashMap<>();
        col3.put("name", "born_at");
        col3.put("data_type", "datetime");

        list.add(col1);
        list.add(col2);
        list.add(col3);

        coreDBDao.createTableIfNotExist("databus_core", "table1", list);
        System.out.println("==> test table created");

        String jsonBody =
                "[{\"name\":\"dave\",\"age\":\"19\",\"born_at\":\"1995/08/11\"}, " +
                "{\"name\":\"dave2\",\"age\":\"20\",\"born_at\":\"1995/08/11\"}, " +
                "{\"name\":\"dave3\",\"age\":\"21\",\"born_at\":\"1995/08/11\"}, " +
                "{\"name\":\"dave4\",\"age\":\"23\",\"born_at\":\"1995/08/11\"}, " +
                "{\"name\":\"dave5\",\"age\":\"24\",\"born_at\":\"1995/08/11\"}, " +
                "{\"name\":\"dave6\",\"age\":\"25\",\"born_at\":\"1995/08/11\"}, " +
                "{\"name\":\"dave7\",\"age\":\"26\",\"born_at\":\"1995/08/11\"}, " +
                "{\"name\":\"dave8\",\"age\":\"29\",\"born_at\":\"1995/08/11\"}, " +
                "{\"name\":\"dave9\",\"age\":\"40\",\"born_at\":\"1995/08/11\"}, " +
                "{\"name\":\"dave10\",\"age\":\"90\",\"born_at\":\"1995/08/11\"}, " +
                "{\"name\":\"fuck\",\"age\":\"16\",\"born_at\":\"2000/04/01\"}]";
        int count = coreDBDao.insertData("databus_core", "table1", jsonBody);
        System.out.println("==> inserted sample data row = " + count);

        String url = baseUri + "/data/databus_core/table1?age_start=25&age_end=40";

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-appkey", "XYZ123");    // use admin to bypass filter
        headers.set("x-mac", MACHelper.calculateMAC(
                "XYZ123", "XYZ123_GET_/api/data/databus_core/table1?age_start=25&age_end=40"));

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<RestResult> resp =
                template.exchange(url, HttpMethod.GET, entity, RestResult.class);

        System.out.println("==> query response: " + resp.getBody());

        assertEquals(ResultType.OK, resp.getBody().getResultType());

        ObjectMapper om = new ObjectMapper();
        List<Map<String, Object>> objects = om.readValue(resp.getBody().getMessage(), List.class);

        assertEquals(4, objects.size());
        System.out.println("==> json resolved ok and query result check ok: ?age_start=xxx&age_end=xxx");

        coreDBDao.dropTableIfExist("databus_core", "table1");
        System.out.println("==> test table dropped");
    }

    @Test
    public void query_data_take_skip_order() throws InternalException, IOException {
        coreDBDao.dropTableIfExist("databus_core", "table1");

        ArrayList<Map<String,Object>> list = new ArrayList<>();
        Map<String,Object> col1 = new HashMap<>();
        col1.put("name", "name");
        col1.put("data_type", "varchar");

        Map<String,Object> col2 = new HashMap<>();
        col2.put("name", "age");
        col2.put("data_type", "smallint unsigned");

        Map<String,Object> col3 = new HashMap<>();
        col3.put("name", "born_at");
        col3.put("data_type", "datetime");

        list.add(col1);
        list.add(col2);
        list.add(col3);

        coreDBDao.createTableIfNotExist("databus_core", "table1", list);
        System.out.println("==> test table created");

        String jsonBody =
                "[{\"name\":\"dave\",\"age\":\"19\",\"born_at\":\"1995/08/11\"}, " +
                "{\"name\":\"dave2\",\"age\":\"20\",\"born_at\":\"1995/08/11\"}, " +
                "{\"name\":\"dave3\",\"age\":\"21\",\"born_at\":\"1995/08/11\"}, " +
                "{\"name\":\"dave4\",\"age\":\"23\",\"born_at\":\"1995/08/11\"}, " +
                "{\"name\":\"dave5\",\"age\":\"24\",\"born_at\":\"1995/08/11\"}, " +
                "{\"name\":\"dave6\",\"age\":\"25\",\"born_at\":\"1995/08/11\"}, " +
                "{\"name\":\"dave7\",\"age\":\"26\",\"born_at\":\"1995/08/11\"}, " +
                "{\"name\":\"dave8\",\"age\":\"29\",\"born_at\":\"1995/08/11\"}, " +
                "{\"name\":\"dave9\",\"age\":\"40\",\"born_at\":\"1995/08/11\"}, " +
                "{\"name\":\"dave10\",\"age\":\"90\",\"born_at\":\"1995/08/11\"}, " +
                "{\"name\":\"fuck\",\"age\":\"16\",\"born_at\":\"2000/04/01\"}]";
        int count = coreDBDao.insertData("databus_core", "table1", jsonBody);
        System.out.println("==> inserted sample data row = " + count);

        String url = baseUri + "/data/databus_core/table1?_skip=2&_take=2&_order=desc&_by=age";

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-appkey", "XYZ123");    // use admin to bypass filter
        headers.set("x-mac", MACHelper.calculateMAC(
                "XYZ123", "XYZ123_GET_/api/data/databus_core/table1?_skip=2&_take=2&_order=desc&_by=age"));

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<RestResult> resp =
                template.exchange(url, HttpMethod.GET, entity, RestResult.class);

        System.out.println("==> query response: " + resp.getBody());

        assertEquals(ResultType.OK, resp.getBody().getResultType());

        ObjectMapper om = new ObjectMapper();
        List<Map<String, Object>> objects = om.readValue(resp.getBody().getMessage(), List.class);
        assertEquals(2, objects.size());

        // om lost origin orders in string format
        int age0 = Integer.valueOf(objects.get(0).get("age").toString());
        int age1 = Integer.valueOf(objects.get(1).get("age").toString());

        assertTrue(age0 < 90 && age1 < 90);  // 90 and 40 should be skipped
        assertTrue(age0 < 40 && age1 < 40);

        System.out.println("==> json resolved ok and query result check ok: ?_take=2&_order=desc&_by=age");

        coreDBDao.dropTableIfExist("databus_core", "table1");
        System.out.println("==> test table dropped");
    }

    @Test
    public void delete_data_simple() throws InternalException, IOException {
        coreDBDao.dropTableIfExist("databus_core", "table1");

        ArrayList<Map<String,Object>> list = new ArrayList<>();
        Map<String,Object> col1 = new HashMap<>();
        col1.put("name", "name");
        col1.put("data_type", "varchar");

        Map<String,Object> col2 = new HashMap<>();
        col2.put("name", "age");
        col2.put("data_type", "smallint unsigned");

        Map<String,Object> col3 = new HashMap<>();
        col3.put("name", "born_at");
        col3.put("data_type", "datetime");

        list.add(col1);
        list.add(col2);
        list.add(col3);

        coreDBDao.createTableIfNotExist("databus_core", "table1", list);
        System.out.println("==> test table created");

        String jsonBody =
                "[{\"name\":\"dave\",\"age\":\"19\",\"born_at\":\"1995/08/11\"}, " +
                "{\"name\":\"dave2\",\"age\":\"20\",\"born_at\":\"1995/08/11\"}, " +
                "{\"name\":\"dave9\",\"age\":\"40\",\"born_at\":\"1995/08/11\"}, " +
                "{\"name\":\"dave10\",\"age\":\"90\",\"born_at\":\"1995/08/11\"}, " +
                "{\"name\":\"fuck\",\"age\":\"16\",\"born_at\":\"2000/04/01\"}]";
        int count = coreDBDao.insertData("databus_core", "table1", jsonBody);
        System.out.println("==> inserted sample data row = " + count);

        String url = baseUri + "/data/databus_core/table1?name=dave";

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-appkey", "XYZ123");    // use admin to bypass filter
        headers.set("x-mac", MACHelper.calculateMAC(
                "XYZ123", "XYZ123_DELETE_/api/data/databus_core/table1?name=dave"));

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<RestResult> resp =
                template.exchange(url, HttpMethod.DELETE, entity, RestResult.class);

        System.out.println("==> delete response: " + resp.getBody());

        assertEquals(ResultType.OK, resp.getBody().getResultType());
        assertEquals("1 rows deleted", resp.getBody().getMessage());

        System.out.println("==> json resolved ok and query result check ok: ?name=dave");

        coreDBDao.dropTableIfExist("databus_core", "table1");
        System.out.println("==> test table dropped");
    }

    @Test
    public void delete_data_start_end() throws InternalException, IOException {
        coreDBDao.dropTableIfExist("databus_core", "table1");

        ArrayList<Map<String,Object>> list = new ArrayList<>();
        Map<String,Object> col1 = new HashMap<>();
        col1.put("name", "name");
        col1.put("data_type", "varchar");

        Map<String,Object> col2 = new HashMap<>();
        col2.put("name", "age");
        col2.put("data_type", "smallint unsigned");

        Map<String,Object> col3 = new HashMap<>();
        col3.put("name", "born_at");
        col3.put("data_type", "datetime");

        list.add(col1);
        list.add(col2);
        list.add(col3);

        coreDBDao.createTableIfNotExist("databus_core", "table1", list);
        System.out.println("==> test table created");

        String jsonBody =
                "[{\"name\":\"dave\",\"age\":\"19\",\"born_at\":\"1995/08/11\"}, " +
                        "{\"name\":\"dave2\",\"age\":\"20\",\"born_at\":\"1995/08/11\"}, " +
                        "{\"name\":\"dave9\",\"age\":\"40\",\"born_at\":\"1995/08/11\"}, " +
                        "{\"name\":\"dave10\",\"age\":\"90\",\"born_at\":\"1995/08/11\"}, " +
                        "{\"name\":\"fuck\",\"age\":\"16\",\"born_at\":\"2000/04/01\"}]";
        int count = coreDBDao.insertData("databus_core", "table1", jsonBody);
        System.out.println("==> inserted sample data row = " + count);

        String url = baseUri + "/data/databus_core/table1?age_start=40&age_end=90";

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-appkey", "XYZ123");    // use admin to bypass filter
        headers.set("x-mac", MACHelper.calculateMAC(
                "XYZ123", "XYZ123_DELETE_/api/data/databus_core/table1?age_start=40&age_end=90"));

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<RestResult> resp =
                template.exchange(url, HttpMethod.DELETE, entity, RestResult.class);

        System.out.println("==> delete response: " + resp.getBody());

        assertEquals(ResultType.OK, resp.getBody().getResultType());
        assertEquals("2 rows deleted", resp.getBody().getMessage());

        System.out.println("==> json resolved ok and query result check ok: ?age_start=40&age_end=90");

        coreDBDao.dropTableIfExist("databus_core", "table1");
        System.out.println("==> test table dropped");
    }

    @Test
    public void insert_simple_data() throws InternalException, JsonProcessingException {
        ArrayList<Map<String,Object>> list = new ArrayList<>();
        Map<String,Object> col1 = new HashMap<>();
        col1.put("name", "name");
        col1.put("data_type", "varchar");

        Map<String,Object> col2 = new HashMap<>();
        col2.put("name", "age");
        col2.put("data_type", "smallint unsigned");

        list.add(col1);
        list.add(col2);
        coreDBDao.createTableIfNotExist("databus_core", "table1", list);
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

        // should be done ok
        assertEquals(ResultType.OK, resp.getBody().getResultType());

        // query back to check result
        Map<String, String[]> p2 = new HashMap<>(); // blank clause
        String result = coreDBDao.queryData("databus_core", "table1", p2);
        System.out.println(result);
        assertEquals("[{\"id\":1,\"name\":\"dave\",\"age\":20}]",result);

        coreDBDao.dropTableIfExist("databus_core", "table1");
        System.out.println("==> test table dropped");
    }

    @Test
    public void update_simple() throws InternalException, JsonProcessingException {
        ArrayList<Map<String,Object>> list = new ArrayList<>();
        Map<String,Object> col1 = new HashMap<>();
        col1.put("name", "name");
        col1.put("data_type", "varchar");
        list.add(col1);

        coreDBDao.createTableIfNotExist("databus_core", "table1", list);
        System.out.println("==> test table created");

        // insert some data
        String jsonBody = "[{\"name\":\"dave\"}, {\"name\":\"fuck\"}]";
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

        // should run ok
        System.out.println("==> response: " + resp.getBody());
        assertEquals(ResultType.OK, resp.getBody().getResultType());

        // get data changed, data should be changed correctly
        Map<String, String[]> p2 = new HashMap<>();
        p2.put("name_not", new String[]{"fuck"});
        String result = coreDBDao.queryData("databus_core", "table1", p2);
        assertTrue(result.contains("dave very 666"));

        // clean
        coreDBDao.dropTableIfExist("databus_core", "table1");
        System.out.println("==> test table dropped");
    }

    @Test
    public void update_not_exist_col() throws InternalException {
        ArrayList<Map<String,Object>> list = new ArrayList<>();
        Map<String,Object> col1 = new HashMap<>();
        col1.put("name", "name");
        col1.put("data_type", "varchar");

        list.add(col1);

        coreDBDao.createTableIfNotExist("databus_core", "table1", list);
        System.out.println("==> test table created");

        String jsonBody = "[{\"name\":\"dave\"}, {\"name\":\"fuck\"}]";
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
        ArrayList<Map<String,Object>> list = new ArrayList<>();
        Map<String,Object> col1 = new HashMap<>();
        col1.put("name", "name");
        col1.put("data_type", "varchar");
        list.add(col1);
        coreDBDao.createTableIfNotExist("databus_core", "table1", list);

        System.out.println("==> test table created");

        String jsonBody = "[{\"name\":\"dave\"}, {\"name\":\"fuck\"}]";
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
