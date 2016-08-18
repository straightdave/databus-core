package com.blueline.databus.core.helper;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.blueline.databus.core.datatype.ColumnInfo;
import com.blueline.databus.core.configtype.DefaultConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


/**
 * 将参数解析成SQL语句
 */
@Component
public class SQLParser {
    private DefaultConfig defaultConfig;

    public SQLParser(DefaultConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
    }

    /**
     * 分析Query String,将其转化为Select-SQL语句的条件clause
     * @param paramMap request获取的parameter Map
     * @return SQL查询clause
     */
    public String parseSQL4Select(Map<String, String[]> paramMap) {
	    // 预处理map,确保包含meta参数
        if (!paramMap.containsKey("_by")) {
            // 默认排序字段是id,这要求库中表都应该含有id字段
            paramMap.put("_by", new String[] { "id" });
        }
        if (!paramMap.containsKey("_order")) {
            paramMap.put("_order", new String[] { "asc" });
        }
        if (!paramMap.containsKey("_skip")) {
            paramMap.put("_skip", new String[] { "0" });
        }
        if (!paramMap.containsKey("_take")) {
            paramMap.put("_take", new String[] {
                defaultConfig.getDefaultTakes()
            });
        }

        StringBuilder sqlQuery = new StringBuilder("WHERE ");

        // 先拼接非meta参数
        paramMap.entrySet()
                .stream()
                .filter(entry -> !entry.getKey().startsWith("_"))
                .forEach(entry -> {
                    String k = entry.getKey();
                    String[] v = entry.getValue();

                    // 对所有 *_start或*_stop 参数,如若同名多值,只支持其第一个值,即取v[0]
                    if (k.endsWith("_start")) {
                        String columnName = k.substring(0, k.length() - "_start".length());
                        sqlQuery.append(String.format("`%s`>='%s' AND ", columnName, v[0]));
                    }
                    else if (k.endsWith("_stop")) {
                        String columnName = k.substring(0, k.length() - "_stop".length());
                        sqlQuery.append(String.format("`%s`<='%s' AND ", columnName, v[0]));
                    }

                    // 对于 *_not;多值可重复拼接
                    else if (k.endsWith("_not")) {
                        String columnName = k.substring(0, k.length() - "_not".length());
                        if (v.length > 1) {
                            sqlQuery.append("(");
                            for (String value : v) {
                                sqlQuery.append(String.format("`%s`<>'%s' AND ", columnName, value));
                            }
                            // 用1=1消除遗留的AND
                            sqlQuery.append("1=1) AND ");
                        }
                        else if (v.length == 1) {
                            sqlQuery.append(String.format("`%s`='%s' AND ", columnName, v[0]));
                        }
                    }

                    // 对于其他普通参数值对;如遇同名多值则重复用OR拼接
                    else {
                        if (v.length > 1) {
                            sqlQuery.append("(");
                            for (String value : v) {
                                sqlQuery.append(String.format("`%s`='%s' OR ", k, value));
                            }
                            // 用1=0消除遗留的OR
                            sqlQuery.append("1=0) AND ");
                        }
                        else if (v.length == 1) {
                            sqlQuery.append(String.format("`%s`='%s' AND ", k, v[0]));
                        }
                    }
                });

        // 在条件clause最后加上1=1,有助于去掉上面拼接时遗留的尾部的AND的功能
        // 并且,在没有条件clause的时候,1=1也可以去掉WHERE的功能
        sqlQuery.append("1=1 ");

        // 最后拼接meta参数
        sqlQuery.append(String.format("ORDER BY `%s` %s ",
                paramMap.get("_by")[0], paramMap.get("_order")[0].toUpperCase()));
        sqlQuery.append(String.format("LIMIT %s,%s",
                paramMap.get("_skip")[0], paramMap.get("_take")[0]));

        return sqlQuery.toString();
	}

    /**
     * 分析Query String,将其转化为Delete-SQL语句的条件clause
     * @param paramMap 从request调用getParamsterMap获取的map
     * @return sql clauses
     */
    public String parseSQL4Delete(Map<String, String[]> paramMap) {

        StringBuilder sqlQuery = new StringBuilder("WHERE ");

        // 本操作忽略meta参数
        paramMap.entrySet()
                .stream()
                .filter(entry -> !entry.getKey().startsWith("_"))
                .forEach(entry -> {
                    String k = entry.getKey();
                    String[] v = entry.getValue();

                    // 对所有 *_start或*_stop 参数,如若同名多值,只支持其第一个值,即取v[0]
                    if (k.endsWith("_start")) {
                        String columnName = k.substring(0, k.length() - "_start".length());
                        sqlQuery.append(String.format("`%s`>='%s' AND ", columnName, v[0]));
                    }
                    else if (k.endsWith("_stop")) {
                        String columnName = k.substring(0, k.length() - "_stop".length());
                        sqlQuery.append(String.format("`%s`<='%s' AND ", columnName, v[0]));
                    }

                    // 对于 *_not;多值可重复拼接
                    else if (k.endsWith("_not")) {
                        String columnName = k.substring(0, k.length() - "_not".length());
                        if (v.length > 1) {
                            sqlQuery.append("(");
                            for (String value : v) {
                                sqlQuery.append(String.format("`%s`<>'%s' AND ", columnName, value));
                            }
                            // 用1=1消除遗留的AND
                            sqlQuery.append("1=1) AND ");
                        }
                        else if (v.length == 1) {
                            sqlQuery.append(String.format("`%s`='%s' AND ", columnName, v[0]));
                        }
                    }

                    // 对于其他普通参数值对;如遇同名多值则重复用OR拼接
                    else {
                        if (v.length > 1) {
                            sqlQuery.append("(");
                            for (String value : v) {
                                sqlQuery.append(String.format("`%s`='%s' OR ", k, value));
                            }
                            // 用1=0消除遗留的OR
                            sqlQuery.append("1=0) AND ");
                        }
                        else if (v.length == 1) {
                            sqlQuery.append(String.format("`%s`='%s' AND ", k, v[0]));
                        }
                    }
                });

        // 在条件clause最后加上1=1,有助于去掉上面拼接时遗留的尾部的AND的功能
        // 并且,在没有条件clause的时候,1=1也可以去掉WHERE的功能
        sqlQuery.append("1=1");

        return sqlQuery.toString();
    }

    /**
     * 从post请求的内容中(这里是body中的json)解析出插入数据的sql语句
     * json是个列表,每个元素代表要插入的一行数值;
     * 各行缺少某些列值的,取'DEFAULT'作为值;
     * 忽略id列和列信息中没有的列;
     * @param jsonBody ServletRequest的getInputStream()的结果
     *                 造型大概是:
     *                 [
     *                     {"name":"dave", "age":"18", "sex":"male"},
     *                     {"name":"mike", "age":"18", "sex":"male", "salary":"20k"},
     *                     ...
     *                 ]
     *
     * @param columnInfoList 列信息
     * @return sql整句 INSERT INTO `%s`.`%s` (... ) VALUES (...), (...), ...
     */
    public String parseSQL4Insert(String jsonBody, final List<ColumnInfo> columnInfoList) {

        // 转换HTTP body传来的JSON数组,结构出错则报IOException
        ObjectMapper om = new ObjectMapper();
        List<Map<String, Object>> inputData = null;
        try {
            inputData = om.readValue(jsonBody, LinkedList.class);
        }
        catch (IOException ex) {
            System.err.println("fuck cannot wrap request data into json!");
        }

        // 从JSON中收集数据,等待解析成sql的list of maps
        List<Map<String, String>> data_to_be_trans = new LinkedList<>();

        // 每次先用columnInfo列表初始化好一个map的模板
        // 填入key(不含id那一列),value先用DEFAULT填充
        Map<String, String> one_row_template = new HashMap<>();
        columnInfoList.stream()
                .filter(col -> !col.getName().equalsIgnoreCase("id"))
                .forEachOrdered(col -> one_row_template.put(col.getName(), "DEFAULT"));

        // 将从JSON解析来的数据作为数据源(map的列表,过滤其中的空map)
        // 用列表每个元素(map)的值,根据key值填充到初始化过的map中
        // 最后将map加到data_to_be_trans这个集合
        inputData.stream()
                .filter(row -> row.size() > 0)
                .forEach(row_map -> {
                    // 从模板复制一份map表示一行数据
                    Map<String, String> one_row = new HashMap<>(one_row_template);
                    row_map.forEach((key, value) -> {
                        // 忽略输入值中不存在的键(列名)
                        if (one_row.keySet().contains(key)) {
                            one_row.put(key, value.toString());
                        }
                    });
                    // 填充好一个map后,加入data这个map的集合
                    data_to_be_trans.add(one_row);
                });

        // 将data(map的列表,一个map代表一行数据)解析成sql语句
        StringBuilder sb = new StringBuilder("INSERT INTO `%s`.`%s` (");

        columnInfoList.stream()
                .filter(col -> !col.getName().equalsIgnoreCase("id"))
                .forEachOrdered(col -> sb.append(String.format("`%s`,", col.getName())));

        // 去掉最后一个逗号,替换成右括号
        removeLastComma(sb, ")");
        sb.append(" VALUES ");

        data_to_be_trans.forEach(row -> {
            sb.append("(");
            columnInfoList.stream()
                    .filter(col -> !col.getName().equalsIgnoreCase("id"))
                    .forEachOrdered(col -> {
                        String value = row.get(col.getName());
                        if (value.equalsIgnoreCase("default")) {
                            // DEFAULT作为mysql关键字不用加引号
                            sb.append(String.format("%s,", value));
                        }
                        else {
                            // 只要值能被mysql解析成相应类型,都可以用单引号包裹
                            sb.append(String.format("'%s',", value));
                        }
                    });
            removeLastComma(sb, "),");
        });

        removeLastComma(sb);
        return sb.toString();
    }

    /**
     * 解析数据更新操作的简单的实现,目前可以实现的更新比较有限
     * @param paramMap
     * @param colName
     * @param colValue
     * @return
     */
    public String parseSQL4Update(Map<String, String[]> paramMap, String colName, String colValue) {
        StringBuilder sb = new StringBuilder("UPDATE `%s`.`%s` SET ");
        paramMap.forEach((k, v) -> sb.append(String.format("`%s`='%s',", k, v[0])));
        removeLastComma(sb);
        sb.append(String.format(" WHERE `%s`='%s'", colName, colValue));
        return sb.toString();
    }

    private void removeLastComma(StringBuilder sb) {
        removeLastComma(sb, "");
    }

    private void removeLastComma(StringBuilder sb, String replacement) {
        int index_of_last_comma = sb.lastIndexOf(",");
        sb.replace(index_of_last_comma, index_of_last_comma + 1, replacement);
    }

    public String parseCreateTableSQL(String dbName, String tableName, String jsonBody) {

        if (StringUtils.isEmpty(jsonBody)) {
            System.err.println("fuck I got no json body!");
        }

        System.out.println(jsonBody);

        List<ColumnInfo> columnInfoList = new LinkedList<>();

        // 转换HTTP body传来的JSON数组,结构出错则报IOException
        ObjectMapper om = new ObjectMapper();
        List<Map<String, String>> rawBody = null;

        try {
            rawBody = om.readValue(jsonBody, List.class);
        }
        catch (IOException ex) {
            System.err.println("fuck cannot wrap request data into json!");
        }

        rawBody.forEach(map -> {
            if (map.containsKey("name") && map.containsKey("type")) {
                String colName = map.get("name");
                String colType = map.get("type").toUpperCase();

                boolean isNullable = true;
                if (map.containsKey("nullable")) {
                    isNullable = map.get("nullable").equalsIgnoreCase("true");
                }

                columnInfoList.add(new ColumnInfo(colName, colType, isNullable));
            }
        });


        StringBuilder sb = new StringBuilder();
        sb.append(String.format("CREATE TABLE `%s`.`%s` (`id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,", dbName, tableName));

        columnInfoList.stream()
                .filter(col -> !col.getName().equalsIgnoreCase("id")) // ignore user-provided id column
                .forEach(col -> {
                    sb.append(String.format("`%s` %s%s,", col.getName(), col.getColumnType(), col.isNullable()? " NULL" : ""));
                });

        removeLastComma(sb, ");");
        return sb.toString();
    }

    public String parseDropTableSQL(String dbName, String tableName) {
        // not use 'IF EXISTS' here to allow exceptions
        return String.format("DROP TABLE `%s`.`%s`", dbName, tableName);
    }
}
