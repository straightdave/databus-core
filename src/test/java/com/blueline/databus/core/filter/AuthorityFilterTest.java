package com.blueline.databus.core.filter;

import com.blueline.databus.core.dao.CoreDBDao;
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
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@IntegrationTest("server.port:8888")
public class AuthorityFilterTest {

    private TestRestTemplate restTemplate = new TestRestTemplate();

    @Autowired
    private CoreDBDao coreDBDao;

    @Test
    public void non_admin_has_no_access() throws InternalException {

        String jsonBody = "[{\"name\":\"name\",\"type\":\"varchar(255)\"}]";
        coreDBDao.createTableIfNotExist("databus_core", "table1", jsonBody);
        System.out.println("===> test table created");

        String dummyApi = "http://localhost:8888/api/data/databus_core/table1?id=1";

        String reqMac = MACHelper.calculateMAC("skey1", "appkey1_GET_/api/data/databus_core/table1?id=1");

        HttpHeaders headers = new HttpHeaders();
        headers.add("x-appkey", "appkey1");
        headers.add("x-mac", reqMac);

        ResponseEntity<RestResult> result = restTemplate.exchange(
                dummyApi, HttpMethod.GET, new HttpEntity<>(headers), RestResult.class);

        System.out.println(result.getBody());

        assertNotEquals(ResultType.OK, result.getBody().getResultType());
        assertTrue(result.getBody().getMessage().contains("No Access"));

        coreDBDao.dropTableIfExist("databus_core", "table1");
        System.out.println("===> test table dropped");
    }
}
