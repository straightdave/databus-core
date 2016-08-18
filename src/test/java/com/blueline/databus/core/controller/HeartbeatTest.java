package com.blueline.databus.core.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;


/**
 * 不需要经过访问限制filter的请求的测试
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@IntegrationTest("server.port:8888")
public class HeartbeatTest {

    // 不知为啥,不能在此处autowire这个对象
    // 根据官方文档,此处autowire这个对象可以自动设置baseUrl和端口
    private TestRestTemplate template = new TestRestTemplate();

    @Test
    public void can_say_hi_not_be_filtered() {
        String hi = template.getForObject("http://localhost:8888/hi", String.class);
        assertEquals("Hi~", hi);
    }

    @Test
    public void should_have_cors_headers() {
        HttpHeaders headers = template.getForEntity("http://localhost:8888/hi", String.class)
                .getHeaders();
        assertTrue(headers.keySet()
                            .stream()
                            .anyMatch(key -> key.toLowerCase().contains("access-control-")));
    }
}