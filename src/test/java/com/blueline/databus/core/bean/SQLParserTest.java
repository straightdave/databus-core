package com.blueline.databus.core.bean;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

// 因为底层涉及到取配置项的值,因此需要连SpringBoot一起run: @RunWith和@SpringBootTest
@RunWith(SpringRunner.class)
@SpringBootTest
public class SQLParserTest {

    @Autowired
    private SQLParser sqlParser;

    @Test
    public void select_parse_simple_query() {
        Map<String, String[]> map = new HashMap<>();
        map.put("id", new String[] { "1" });
        map.put("name", new String[] { "dave" });
        // 普通参数拼接是按照先入后出的

        String result = sqlParser.parseQueryString4Select(map);
        assertFalse(StringUtils.isEmpty(result));

        System.out.println(result);
        assertEquals("WHERE `name`='dave' AND `id`='1' AND 1=1 ORDER BY `id` ASC LIMIT 0,10", result);
    }

    @Test
    public void select_parse_multiple_data() {
        Map<String, String[]> map = new HashMap<>();
        map.put("id", new String[] { "1", "2" });

        String result = sqlParser.parseQueryString4Select(map);
        assertFalse(StringUtils.isEmpty(result));

        System.out.println(result);
        assertEquals("WHERE `id`='1' AND `id`='2' AND 1=1 ORDER BY `id` ASC LIMIT 0,10", result);
    }

    @Test
    public void select_parse_only_meta() {
        Map<String, String[]> map = new HashMap<>();
        map.put("_by", new String[] { "name" });
        map.put("_skip", new String[] { "50" });
        map.put("_take", new String[] { "100" });

        String result = sqlParser.parseQueryString4Select(map);
        assertFalse(StringUtils.isEmpty(result));

        System.out.println(result);
        assertEquals("WHERE 1=1 ORDER BY `name` ASC LIMIT 50,100", result);
    }

    @Test
    public void select_parse_start_stop() {
        Map<String, String[]> map = new HashMap<>();
        map.put("id_start", new String[] { "10" });
        map.put("id_stop", new String[] { "50", "20" });
        map.put("_take", new String[] { "100" });

        String result = sqlParser.parseQueryString4Select(map);
        assertFalse(StringUtils.isEmpty(result));

        System.out.println(result);
        assertEquals("WHERE `id`<='50' AND `id`>='10' AND 1=1 ORDER BY `id` ASC LIMIT 0,100", result);
    }
}
