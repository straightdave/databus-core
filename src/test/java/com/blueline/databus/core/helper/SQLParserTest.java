package com.blueline.databus.core.helper;

import com.blueline.databus.core.datatype.ColumnInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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

        String result = sqlParser.parseSQL4Select(map);
        assertFalse(StringUtils.isEmpty(result));

        System.out.println(result);
        assertEquals("WHERE `name`='dave' AND `id`='1' AND 1=1 ORDER BY `id` ASC LIMIT 0,10", result);
    }

    @Test
    public void select_parse_multiple_data() {
        Map<String, String[]> map = new HashMap<>();
        map.put("id", new String[] { "1", "2" });

        String result = sqlParser.parseSQL4Select(map);
        assertFalse(StringUtils.isEmpty(result));

        System.out.println(result);
        assertEquals("WHERE (`id`='1' OR `id`='2' OR 1=0) AND 1=1 ORDER BY `id` ASC LIMIT 0,10", result);
    }

    @Test
    public void select_parse_only_meta() {
        Map<String, String[]> map = new HashMap<>();
        map.put("_by", new String[] { "name" });
        map.put("_skip", new String[] { "50" });
        map.put("_take", new String[] { "100" });

        String result = sqlParser.parseSQL4Select(map);
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

        String result = sqlParser.parseSQL4Select(map);
        assertFalse(StringUtils.isEmpty(result));

        System.out.println(result);
        assertEquals("WHERE `id`<='50' AND `id`>='10' AND 1=1 ORDER BY `id` ASC LIMIT 0,100", result);
    }

    @Test
    public void delete_simple() {
        Map<String, String[]> map = new HashMap<>();
        map.put("id", new String[] { "1", "2" });

        String result = sqlParser.parseSQL4Delete(map);
        assertFalse("SQL should not be blank", StringUtils.isEmpty(result));

        System.out.println(result);
        assertEquals("WHERE (`id`='1' OR `id`='2' OR 1=0) AND 1=1", result);
    }

    @Test
    public void insert_parse_right() throws IOException {
        String jsonBody = "[{\"name\" : \"dave\", \"age\" : \"18\"}, {\"name\": \"frank\", \"age\" : \"11\"}]";

        List<ColumnInfo> columnInfoList = new LinkedList<>();
        columnInfoList.add(new ColumnInfo("id", "int", 1));
        columnInfoList.add(new ColumnInfo("name", "varchar", 2));
        columnInfoList.add(new ColumnInfo("age", "smallint", 3));

        String result = sqlParser.parseSQL4Insert(jsonBody, columnInfoList);

        assertFalse(StringUtils.isEmpty(result));
        assertEquals("INSERT INTO `%s`.`%s` (`name`,`age`) VALUES ('dave','18'),('frank','11')", result);
        System.out.println(result);
    }

    @Test
    public void insert_parse_bad() throws IOException {
        String jsonBody = "[{\"name\" : \"dirk\", \"age\" : 18}, {}]";

        List<ColumnInfo> columnInfoList = new LinkedList<>();
        columnInfoList.add(new ColumnInfo("id", "int", 1));
        columnInfoList.add(new ColumnInfo("name", "varchar", 2));
        columnInfoList.add(new ColumnInfo("age", "smallint", 3));

        String result = sqlParser.parseSQL4Insert(jsonBody, columnInfoList);

        assertFalse(StringUtils.isEmpty(result));
        assertEquals("INSERT INTO `%s`.`%s` (`name`,`age`) VALUES ('dirk','18')", result);

        System.out.println(result);
    }

    @Test
    public void update_parse_multi_value() throws IOException {
        Map<String, String[]> map = new HashMap<>();
        map.put("name", new String[] { "1", "2" });
        map.put("sex", new String[] {"female"});  // 先入后出顺序

        String result = sqlParser.parseSQL4Update(map, "id", "10");
        assertFalse("SQL should not be blank", StringUtils.isEmpty(result));

        System.out.println(result);
        assertEquals("UPDATE `%s`.`%s` SET `sex`='female',`name`='1' WHERE `id`='10'", result);
    }

    @Test
    public void createTable_parse() throws IOException {

        String jsonBody = "[{\"name\":\"col1\",\"type\":\"int unsigned\",\"nullable\":\"true\"}]";

        String result = sqlParser.parseCreateTableSQL("db1", "tb1", jsonBody);
        assertFalse("SQL should not be blank", StringUtils.isEmpty(result));
        System.out.println(result);
    }

    @Test
    public void createTable_parse_more_complex() throws IOException {

        String jsonBody = "[{\"name\":\"id\",\"type\":\"int unsigned\",\"nullable\":\"true\"}," +
                "{\"name\":\"id\",\"type\":\"int unsigned\",\"nullable\":\"true\"}," +
                "{\"name\":\"sex\",\"type\":\"varchar\",\"nullable\":\"false\"}" +
                "]";

        String result = sqlParser.parseCreateTableSQL("db1", "tb1", jsonBody);
        assertFalse("SQL should not be blank", StringUtils.isEmpty(result));
        System.out.println(result);
    }
}
