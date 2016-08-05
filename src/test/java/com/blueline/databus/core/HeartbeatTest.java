package com.blueline.databus.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HeartbeatTest {

    // TODO: 不知为啥,不能在此处autowire这个对象
    private TestRestTemplate template = new TestRestTemplate();

    @Test
    public void can_say_hi() {
        String hi = template.getForObject("http://localhost:8888/hi", String.class);
        assertEquals("Hi~", hi);
    }
}