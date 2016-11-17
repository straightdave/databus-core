package com.blueline.databus.core.controller;

import com.blueline.databus.core.dao.CoreDBDao;
import com.blueline.databus.core.dao.SysDBDao;
import com.blueline.databus.core.datatype.*;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@IntegrationTest("server.port:8888")
public class DDLControllerTest {

    @Autowired
    private CoreDBDao coreDBDao;

    @Autowired
    private SysDBDao sysDBDao;

    @Test
    public void drop_non_exist_table() {
        // clean same name table
        coreDBDao.dropTableIfExist("databus_core", "tb1");

        TestRestTemplate template = new TestRestTemplate();
        String url = "http://localhost:8888/api/def/databus_core/tb1";

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-appkey", "XYZ123");
        headers.set("x-mac", MACHelper.calculateMAC(
                "XYZ123", "XYZ123_DELETE_/api/def/databus_core/tb1"));

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<RestResult> resp =
                template.exchange(url, HttpMethod.DELETE, entity, RestResult.class);

        System.out.println(resp.getBody());
        assertNotEquals(ResultType.OK, resp.getBody().getResultType());
    }

    @Test
    public void drop_existing_table() throws IOException, InternalException {

        // ensure table exists
        ArrayList<Map<String,Object>> list = new ArrayList<>();
        Map<String,Object> col = new HashMap<>();
        col.put("name","username");
        col.put("data_type","varchar");
        list.add(col);
        coreDBDao.createTableIfNotExist("databus_core", "tb1", list);

        TestRestTemplate template = new TestRestTemplate();
        String url = "http://localhost:8888/api/def/databus_core/tb1";

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-appkey", "XYZ123");
        headers.set("x-mac", MACHelper.calculateMAC(
                "XYZ123", "XYZ123_DELETE_/api/def/databus_core/tb1"));

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<RestResult> resp =
                template.exchange(url, HttpMethod.DELETE, entity, RestResult.class);

        System.out.println(resp.getBody().getMessage());
        assertEquals(ResultType.OK, resp.getBody().getResultType());
    }

    @Test
    public void can_create_table() throws InternalException {
        // clean same name table and meta data
        coreDBDao.dropTableIfExist("databus_core", "tb1");
        sysDBDao.doAfterTableDropped("databus_core", "tb1");
        // add test client
        sysDBDao.deleteClient("test_client");
        sysDBDao.createClient("{\"name\":\"test_client\"}");

        TestRestTemplate template = new TestRestTemplate();
        String url = "http://localhost:8888/api/def/databus_core/tb1";

        // to have a comment on first column is also ok
        String body = "{ \"owner_name\"  : \"test_client\"," +
                      "  \"description\" : \"this is a table\"," +
                      "  \"columns\"     : [{\"comment\":\"haha\",\"name\":\"name\",\"data_type\":\"varchar\",\"nullable\":false,\"unique\":true}," +
                      "                     {\"name\":\"age\", \"data_type\":\"smallint unsigned\"}]}";

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-appkey", "XYZ123");
        headers.set("Content-Type", "application/json");
        headers.set("x-mac", MACHelper.calculateMAC(
                "XYZ123", "XYZ123_POST_/api/def/databus_core/tb1"));

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<RestResult> resp =
                template.exchange(url, HttpMethod.POST, entity, RestResult.class);

        System.out.println(resp.getBody());
        assertEquals(ResultType.OK, resp.getBody().getResultType());

        // check meta data inserted in db
        TableInfo table = sysDBDao.getTableInfoBy("databus_core", "tb1");
        assertNotNull(table); // should get 1 back
        assertEquals("tb1", table.getName());
        assertEquals("this is a table", table.getDescription());

        // CRUD interfaces should be generated
        List<InterfaceInfo> interfaces = sysDBDao.getInterfaceInfoByTable("databus_core", "tb1");
        assertEquals(4, interfaces.size());

        // clean
        coreDBDao.dropTableIfExist("databus_core", "tb1");
        sysDBDao.doAfterTableDropped("databus_core", "tb1");
        sysDBDao.deleteClient("test_client");
    }

    @Test
    public void test_get_columns() throws InternalException, IOException {
        // ensure table exists
        ArrayList<Map<String,Object>> list = new ArrayList<>();

        Map<String,Object> col1 = new HashMap<>();
        col1.put("name","name");
        col1.put("data_type","varchar");

        Map<String,Object> col2 = new HashMap<>();
        col2.put("name","age");
        col2.put("data_type","smallint unsigned");
        col2.put("comment", "the age of history"); // add a comment here

        Map<String,Object> col3 = new HashMap<>();
        col3.put("name","born_at");
        col3.put("data_type","datetime");
        col3.put("nullable",true);

        list.add(col1);
        list.add(col2);
        list.add(col3);
        coreDBDao.createTableIfNotExist("databus_core", "tb1", list);

        // make request
        TestRestTemplate template = new TestRestTemplate();
        String url = "http://localhost:8888/api/def/databus_core/tb1/columns";

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-appkey", "XYZ123");
        headers.set("x-mac", MACHelper.calculateMAC(
                "XYZ123", "XYZ123_GET_/api/def/databus_core/tb1/columns"));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<RestResult> resp =
                template.exchange(url, HttpMethod.GET, entity, RestResult.class);

        // check request done
        assertEquals(ResultType.OK, resp.getBody().getResultType());

        // check result
        String columns_info = resp.getBody().getMessage();
        System.out.println(columns_info);

        assertNotNull(columns_info.contains("age of history"));

        // clean
        coreDBDao.dropTableIfExist("databus_core", "tb1");
    }
}
