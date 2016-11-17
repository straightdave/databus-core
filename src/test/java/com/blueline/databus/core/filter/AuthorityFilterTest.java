package com.blueline.databus.core.filter;

import com.blueline.databus.core.dao.AclCacheService;
import com.blueline.databus.core.dao.CoreDBDao;
import com.blueline.databus.core.dao.SysDBDao;
import com.blueline.databus.core.datatype.ClientInfo;
import com.blueline.databus.core.datatype.ResultType;
import com.blueline.databus.core.exception.InternalException;
import com.blueline.databus.core.helper.MACHelper;
import com.blueline.databus.core.datatype.RestResult;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@IntegrationTest("server.port:8888")
public class AuthorityFilterTest {

    private TestRestTemplate restTemplate = new TestRestTemplate();

    @Autowired
    private CoreDBDao coreDBDao;

    @Autowired
    private SysDBDao sysDBDao;

    @Autowired
    private AclCacheService aclCacheService;

    @Test
    public void non_admin_has_no_access() throws InternalException {
        // clean acl cache
        aclCacheService.flushDB();

        // create test table
        ArrayList<Map<String,Object>> list = new ArrayList<>();
        Map<String,Object> col1 = new HashMap<>();
        col1.put("name", "name");
        col1.put("type", "varchar(20)");

        Map<String,Object> col2 = new HashMap<>();
        col2.put("name", "age");
        col2.put("type", "smallint unsigned");

        Map<String,Object> col3 = new HashMap<>();
        col3.put("name", "born_at");
        col3.put("type", "datetime");

        list.add(col1);
        list.add(col2);
        list.add(col3);

        coreDBDao.createTableIfNotExist("databus_core", "table1", list);
        System.out.println("===> test table created");

        // create test client with no relationship to such table
        sysDBDao.deleteClient("test_client");
        sysDBDao.createClient("{\"name\":\"test_client\"}");

        ClientInfo c = sysDBDao.getClientByName("test_client");

        String dummyApi = "http://localhost:8888/api/data/databus_core/table1?id=1";
        String reqMac = MACHelper.calculateMAC(c.getSKey(), c.getAppKey() + "_GET_/api/data/databus_core/table1?id=1");

        HttpHeaders headers = new HttpHeaders();
        headers.add("x-appkey", c.getAppKey());
        headers.add("x-mac", reqMac);

        ResponseEntity<RestResult> result = restTemplate.exchange(
                dummyApi, HttpMethod.GET, new HttpEntity<>(headers), RestResult.class);

        System.out.println(result.getBody());

        assertNotEquals(ResultType.OK, result.getBody().getResultType());
        assertTrue(result.getBody().getMessage().contains("No Access"));

        // clean
        coreDBDao.dropTableIfExist("databus_core", "table1");
        System.out.println("===> test table dropped");
        sysDBDao.deleteClient("test_client");
    }
}
