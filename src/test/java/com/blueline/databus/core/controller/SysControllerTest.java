package com.blueline.databus.core.controller;

import com.blueline.databus.core.dao.CoreDBDao;
import com.blueline.databus.core.dao.SysDBDao;
import com.blueline.databus.core.datatype.ClientInfo;
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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.net.URLEncoder.encode;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@IntegrationTest("server.port:8888")
public class SysControllerTest {
    private TestRestTemplate template = new TestRestTemplate();
    private final String baseUri = "http://localhost:8888/api/sys";

    @Autowired
    private SysDBDao sysDBDao;

    @Autowired
    private CoreDBDao coreDBDao;

    @Test
    public void get_all_clients() throws InternalException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-appkey", "XYZ123");    // use admin to bypass filter
        headers.set("x-mac", MACHelper.calculateMAC(
                "XYZ123", "XYZ123_GET_/api/sys/clients"));

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<RestResult> resp =
                template.exchange(baseUri + "/clients", HttpMethod.GET, entity, RestResult.class);

        System.out.println(resp.getBody());
        assertEquals(ResultType.OK, resp.getBody().getResultType());
    }

    @Test
    public void get_client_by_name() throws InternalException {
        // clean and create test client
        sysDBDao.deleteClient("test_client");
        sysDBDao.createClient("{\"name\":\"test_client\"}");

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-appkey", "XYZ123");
        headers.set("x-mac", MACHelper.calculateMAC(
                "XYZ123", "XYZ123_GET_/api/sys/client/test_client"));

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<RestResult> resp =
                template.exchange(baseUri + "/client/test_client", HttpMethod.GET, entity, RestResult.class);

        System.out.println(resp.getBody());
        assertEquals(ResultType.OK, resp.getBody().getResultType());

        // clean
        sysDBDao.deleteClient("test_client");
    }

    @Test
    public void get_tables_by_client() throws InternalException {
        // clean client and tables
        sysDBDao.deleteClient("test_client");
        coreDBDao.dropTableIfExist("databus_core", "test_table");
        sysDBDao.doAfterTableDropped("databus_core", "test_table");

        // new client
        sysDBDao.createClient("{\"name\":\"test_client\"}");

        // new data table and meta table
        ArrayList<Map<String,Object>> list = new ArrayList<>();
        Map<String,Object> col = new HashMap<>();
        col.put("name","username");
        col.put("data_type","varchar");
        list.add(col);
        coreDBDao.createTableIfNotExist("databus_core", "test_table", list);
        sysDBDao.doAfterTableCreated("databus_core", "test_table", "test_client", "a good table");

        // insert some data
        coreDBDao.insertData("databus_core", "test_table", "[{\"username\":\"dave\"}]");

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-appkey", "XYZ123");
        headers.set("x-mac", MACHelper.calculateMAC(
                "XYZ123", "XYZ123_GET_/api/sys/client/test_client/tables"));
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<RestResult> resp =
                template.exchange(baseUri + "/client/test_client/tables", HttpMethod.GET, entity, RestResult.class);

        System.out.println(resp.getBody());
        assertEquals(ResultType.OK, resp.getBody().getResultType());

        // clean
        sysDBDao.deleteClient("test_client");
        coreDBDao.dropTableIfExist("databus_core", "test_table");
        sysDBDao.doAfterTableDropped("databus_core", "test_table");
    }

    @Test
    public void get_interface_by_table() throws InternalException {
        // clean and create test client
        sysDBDao.deleteClient("test_client");
        sysDBDao.createClient("{\"name\":\"test_client\"}");

        // create sample interfaces
        // and now test_client has 4 interfaces/acls
        sysDBDao.doAfterTableCreated("databus_core", "test_table", "test_client", "");

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-appkey", "XYZ123");
        headers.set("x-mac", MACHelper.calculateMAC(
                "XYZ123", "XYZ123_GET_/api/sys/interfaces/databus_core/test_table"));

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<RestResult> resp =
                template.exchange(baseUri + "/interfaces/databus_core/test_table", HttpMethod.GET, entity, RestResult.class);

        System.out.println(resp.getBody());
        assertEquals(ResultType.OK, resp.getBody().getResultType());

        // clean
        sysDBDao.deleteClient("test_client");
        sysDBDao.doAfterTableDropped("databus_core", "test_table");
    }

    @Test
    public void get_acl_for_client() throws InternalException {
        // clean and create test client
        sysDBDao.deleteClient("test_client");
        sysDBDao.createClient("{\"name\":\"test_client\"}");

        // create sample interfaces
        // and now test_client has 4 interfaces/acls
        sysDBDao.doAfterTableCreated("databus_core", "test_table", "test_client", "");

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-appkey", "XYZ123");
        headers.set("x-mac", MACHelper.calculateMAC(
                "XYZ123", "XYZ123_GET_/api/sys/client/test_client/acl"));

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<RestResult> resp =
                template.exchange(baseUri + "/client/test_client/acl", HttpMethod.GET, entity, RestResult.class);

        System.out.println(resp.getBody());
        assertNotEquals(ResultType.ERROR, resp.getBody().getResultType());

        // clean
        sysDBDao.deleteClient("test_client");
        sysDBDao.doAfterTableDropped("databus_core", "test_table");
    }

    @Test
    public void dump_acl_cache() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-appkey", "XYZ123");
        headers.set("x-mac", MACHelper.calculateMAC(
                "XYZ123", "XYZ123_GET_/api/sys/acl_cache"));

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<RestResult> resp =
                template.exchange(baseUri + "/acl_cache", HttpMethod.GET, entity, RestResult.class);

        System.out.println(resp.getBody());
        assertEquals(ResultType.OK, resp.getBody().getResultType());
    }

    @Test
    public void dump_call_record() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-appkey", "XYZ123");
        headers.set("x-mac", MACHelper.calculateMAC(
                "XYZ123", "XYZ123_GET_/api/sys/call_records"));

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<RestResult> resp =
                template.exchange(baseUri + "/call_records", HttpMethod.GET, entity, RestResult.class);

        System.out.println(resp.getBody());
        assertEquals(ResultType.OK, resp.getBody().getResultType());
    }

    @Test
    public void test_create_client_all_default() {

        // clean client
        sysDBDao.deleteClient("test_client");

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-appkey", "XYZ123");
        headers.set("x-mac", MACHelper.calculateMAC(
                "XYZ123", "XYZ123_POST_/api/sys/clients"));

        String jsonBody = "{\"name\" : \"test_client\"}";

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
        ResponseEntity<RestResult> resp =
                template.exchange(baseUri + "/clients", HttpMethod.POST, entity, RestResult.class);

        System.out.println(resp.getBody());
        assertEquals(ResultType.OK, resp.getBody().getResultType());

        ClientInfo new_c = sysDBDao.getClientByName("test_client");
        assertNotNull(new_c);
        assertEquals("test_client", new_c.getName());
        assertEquals("test_client", new_c.getDisplayName());
        assertEquals("", new_c.getDescription());
        assertEquals("web", new_c.getClientType());
        assertEquals("internal", new_c.getClientCategory());
        assertEquals("", new_c.getVendorExt());
        assertTrue(new_c.getAppKey().length() > 0);
        assertTrue(new_c.getSKey().length() > 0);

        // clean client
        sysDBDao.deleteClient("test_client");
    }

    @Test
    public void test_create_client() {

        // clean client
        sysDBDao.deleteClient("test_client");

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-appkey", "XYZ123");
        headers.set("x-mac", MACHelper.calculateMAC(
                "XYZ123", "XYZ123_POST_/api/sys/clients"));

        String jsonBody = "{\"name\"         : \"test_client\"," +
                          " \"display_name\" : \"george bush\"," +
                          " \"description\"  : \"such a dork\"," +
                          " \"type\"         : \"ios\"," +
                          " \"category\"     : \"business\"}";

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
        ResponseEntity<RestResult> resp =
                template.exchange(baseUri + "/clients", HttpMethod.POST, entity, RestResult.class);

        System.out.println(resp.getBody());
        assertEquals(ResultType.OK, resp.getBody().getResultType());

        ClientInfo new_c = sysDBDao.getClientByName("test_client");
        assertNotNull(new_c);
        assertEquals("test_client", new_c.getName());
        assertEquals("george bush", new_c.getDisplayName());
        assertEquals("such a dork", new_c.getDescription());
        assertEquals("ios", new_c.getClientType());
        assertEquals("business", new_c.getClientCategory());
        assertEquals("", new_c.getVendorExt());
        assertTrue(new_c.getAppKey().length() > 0);
        assertTrue(new_c.getSKey().length() > 0);

        // clean client
        sysDBDao.deleteClient("test_client");
    }

    @Test
    public void test_create_client_no_name_but_vendor_name() {

        // clean client
        sysDBDao.deleteClient("test_client");

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-appkey", "XYZ123");
        headers.set("x-mac", MACHelper.calculateMAC(
                "XYZ123", "XYZ123_POST_/api/sys/clients"));

        String jsonBody = "{\"vendor_name\":\"test_client\"}";

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
        ResponseEntity<RestResult> resp =
                template.exchange(baseUri + "/clients", HttpMethod.POST, entity, RestResult.class);

        System.out.println(resp.getBody());
        assertEquals(ResultType.OK, resp.getBody().getResultType());

        ClientInfo new_c = sysDBDao.getClientByName("test_client");
        assertNotNull(new_c);
        assertEquals("test_client", new_c.getName());
        assertEquals("test_client", new_c.getDisplayName());
        assertEquals("test_client", new_c.getVendorName());
        assertTrue(new_c.getAppKey().length() > 0);
        assertTrue(new_c.getSKey().length() > 0);

        // clean client
        sysDBDao.deleteClient("test_client");
    }

    @Test
    public void test_get_table_info() throws InternalException {

        // clean client and tables
        sysDBDao.deleteClient("test_client");
        coreDBDao.dropTableIfExist("databus_core", "test_table");
        sysDBDao.doAfterTableDropped("databus_core", "test_table");

        // new client
        sysDBDao.createClient("{\"name\":\"test_client\"}");

        // new data table and meta table
        ArrayList<Map<String,Object>> list = new ArrayList<>();
        Map<String,Object> col = new HashMap<>();
        col.put("name","username");
        col.put("data_type","varchar");
        list.add(col);
        coreDBDao.createTableIfNotExist("databus_core", "test_table", list);
        sysDBDao.doAfterTableCreated("databus_core", "test_table", "test_client", "a good table");

        // insert some data
        coreDBDao.insertData("databus_core", "test_table", "[{\"username\":\"dave\"}]");

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-appkey", "XYZ123");
        headers.set("x-mac", MACHelper.calculateMAC(
                "XYZ123", "XYZ123_GET_/api/sys/table/databus_core/test_table"));
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<RestResult> resp =
                template.exchange(baseUri + "/table/databus_core/test_table", HttpMethod.GET, entity, RestResult.class);

        System.out.println(resp.getBody());
        assertEquals(ResultType.OK, resp.getBody().getResultType());

        // clean
        sysDBDao.deleteClient("test_client");
        coreDBDao.dropTableIfExist("databus_core", "test_table");
        sysDBDao.doAfterTableDropped("databus_core", "test_table");
    }

    @Test
    public void test_grant() throws InternalException {

        // prepare client
        sysDBDao.deleteClient("test_client");
        sysDBDao.createClient("{\"name\":\"test_client\"}");


        // make request (grant acl to client2), assuming interface = 1 (any id is ok)
        // no real interface
        TestRestTemplate template = new TestRestTemplate();
        String url = "http://localhost:8888/api/sys/interface/1/grant_to/test_client";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/x-www-form-urlencoded");
        headers.set("x-appkey", "XYZ123");
        headers.set("x-mac", MACHelper.calculateMAC(
                "XYZ123", "XYZ123_POST_/api/sys/interface/1/grant_to/test_client"));

        String body = "duration=11002200";

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<RestResult> resp =
                template.exchange(url, HttpMethod.POST, entity, RestResult.class);

        System.out.println(resp.getBody());
        assertTrue(resp.getBody().getMessage().contains("duration{11002200}"));

        // clean
        sysDBDao.revokeInterfaceFromClient(1,"test_client");
        sysDBDao.deleteClient("test_client");
    }

}
