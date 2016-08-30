package com.blueline.databus.core.helper;

import java.io.IOException;
import java.util.*;

import com.blueline.databus.core.datatype.ColumnInfo;
import com.blueline.databus.core.exception.InternalException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


/**
 * 将参数解析成SQL语句
 */
@Component
public class SQLParser {

    @Value("${default.defaultTakes}")
    private String defaultTakes;

    /**
     * 分析Query String,将其转化为Select-SQL语句的条件clause
     * @param paramMap request获取的parameter Map
     * @return SQL查询clause
     */
    public String parseSQL4Select(final Map<String, String[]> paramMap) {

        // copy param map
        Map<String, String[]> p2 = new HashMap<>();
        paramMap.forEach((k, v) -> p2.put(k, v));

        // 预处理map,确保包含必须的meta参数
        if (!p2.containsKey("_by")) {
            // 默认排序字段是id,这要求表应该含有字段id
            p2.put("_by", new String[] {"id"});
        }
        if (!p2.containsKey("_order")) {
            // 默认升序
            p2.put("_order", new String[] {"asc"});
        }
        if (!p2.containsKey("_skip")) {
            p2.put("_skip", new String[] {"0"});
        }
        if (!p2.containsKey("_take")) {
            p2.put("_take", new String[] {
                StringUtils.isEmpty(defaultTakes) ? "10" : defaultTakes
            });
        }

        StringBuilder sqlQuery = new StringBuilder("WHERE ");

        // 先拼接非meta参数
        p2.entrySet()
            .stream()
            .filter(entry -> !entry.getKey().startsWith("_"))
            .forEach(entry -> {
                String k = entry.getKey();
                String[] v = entry.getValue();

                // 对所有 *_start或*_stop 参数,如若同名多值,只支持其第一个值,即取v[0]
                if (k.endsWith("_start") || k.endsWith("_begin") || k.endsWith("_gt")) {
                    String columnName = k.substring(0, k.lastIndexOf("_"));
                    sqlQuery.append(String.format("`%s`>='%s' AND ", columnName, v[0]));
                }
                else if (k.endsWith("_stop") || k.endsWith("_end") || k.endsWith("_lt")) {
                    String columnName = k.substring(0, k.lastIndexOf("_"));
                    sqlQuery.append(String.format("`%s`<='%s' AND ", columnName, v[0]));
                }

                // 对于 *_not;多值可重复拼接
                else if (k.endsWith("_not")) {
                    String columnName = k.substring(0, k.lastIndexOf("_"));
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
                p2.get("_by")[0], p2.get("_order")[0].toUpperCase()));
        sqlQuery.append(String.format("LIMIT %s,%s",
                p2.get("_skip")[0], p2.get("_take")[0]));

        return sqlQuery.toString();
	}

    /**
     * 分析Query String,将其转化为Delete-SQL语句的条件clause
     * @param paramMap 从request调用getParamsterMap获取的map(为空则返回"WHERE 1=0",确保数据安全)
     * @return sql clauses
     */
    public String parseSQL4Delete(final Map<String, String[]> paramMap) {

        if (paramMap.size() < 1) {
            return "WHERE 1=0"; // 不附加参数的情况非常危险,这里做不删除处理
        }

        StringBuilder sqlQuery = new StringBuilder("WHERE ");

        // 本操作忽略meta参数
        paramMap.entrySet()
                .stream()
                .filter(entry -> !entry.getKey().startsWith("_"))
                .forEach(entry -> {
                    String k = entry.getKey();
                    String[] v = entry.getValue();

                    // 对所有 *_start或*_stop 参数,如若同名多值,只支持其第一个值,即取v[0]
                    if (k.endsWith("_start") || k.endsWith("_begin") || k.endsWith("_gt")) {
                        String columnName = k.substring(0, k.lastIndexOf("_"));
                        sqlQuery.append(String.format("`%s`>='%s' AND ", columnName, v[0]));
                    }
                    else if (k.endsWith("_stop") || k.endsWith("_end") || k.endsWith("_lt")) {
                        String columnName = k.substring(0, k.lastIndexOf("_"));
                        sqlQuery.append(String.format("`%s`<='%s' AND ", columnName, v[0]));
                    }

                    // 对于 *_not;多值可重复拼接
                    else if (k.endsWith("_not")) {
                        String columnName = k.substring(0, k.lastIndexOf("_"));
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
     *                 <pre>
     *                 <code>
     *                 [
     *                     {"name":"dave", "age":"18", "sex":"male"},
     *                     {"name":"mike", "age":"18", "sex":"male", "salary":"20k"},
     *                     ...
     *                 ]
     *                 </code>
     *                 </pre>
     *
     * @param columnInfoList 列信息
     * @return sql整句 <code>INSERT INTO `%s`.`%s` (... ) VALUES (...), (...), ...</code>
     * @throws InternalException 内部异常
     */
    public String parseSQL4Insert(String jsonBody, final List<ColumnInfo> columnInfoList)
            throws InternalException {

        // 转换HTTP body传来的JSON数组,结构出错则报IOException
        ObjectMapper om = new ObjectMapper();
        List<Map<String, Object>> inputData = null;
        try {
            inputData = om.readValue(jsonBody, LinkedList.class);
        }
        catch (IOException ex) {
            System.err.println("fuck cannot wrap request data into json!");
            throw new InternalException("cannot wrap request body into json");
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
     * update符合条件的表的数据。条件就是后两个路径参数colName和colValue
     * 这两个参数组成UPDATE语句中的 <code>"WHERE `colName` = 'colValue'"</code> 条件句
     * 修改信息是在body中的json字符串,形式大概像insert所需类似:
     *
     * <code> [{"name":"dave", "age":"18", "sex":"male"}] </code>
     *
     * 只不过,只有第一个json元素值会拿来使用
     * 它组成UPDATE语句中的 "SET `xxx` = 'yyy' " 部分
     * 记住,目前不过滤不存在的字段。如果条件和修改信息中有不存在的字段,都会导致最终的错误返回。
     *
     * @param colName 列名
     * @param colValue 列值(作为修改条件)
     * @param jsonBody 请求提内的修改信息的json
     * @return SQL命令
     * @throws InternalException 内部异常
     */
    public String parseSQL4Update(String colName, String colValue, String jsonBody)
            throws InternalException {

        // 转换HTTP body传来的JSON数组,结构出错则报IOException
        ObjectMapper om = new ObjectMapper();
        List<Map<String, Object>> inputData;
        try {
            inputData = om.readValue(jsonBody, LinkedList.class);
        }
        catch (IOException ex) {
            System.err.println("fuck cannot wrap request data into json!");
            throw new InternalException("failed to parse json input");
        }

        if (inputData.size() > 0) {
            StringBuilder sb = new StringBuilder("UPDATE `%s`.`%s` SET ");
            inputData.get(0).forEach((k, v) -> sb.append(String.format("`%s`='%s',", k, v)));
            removeLastComma(sb);
            sb.append(String.format(" WHERE `%s`='%s'", colName, colValue));
            return sb.toString();
        }
        else {
            throw new InternalException("no update clause found in json body");
        }
    }

    /**
     * 处理StringBuilder,去除尾部逗号
     * @param sb 需要修改的StringBuilder实例
     */
    private void removeLastComma(StringBuilder sb) {
        removeLastComma(sb, "");
    }

    /**
     * 处理StringBuilder,将尾部逗号替换为其他字符串
     * @param sb 需要修改的StringBuilder实例
     * @param replacement 替换字符串
     */
    private void removeLastComma(StringBuilder sb, String replacement) {
        int index_of_last_comma = sb.lastIndexOf(",");
        sb.replace(index_of_last_comma, index_of_last_comma + 1, replacement);
    }



    //
    // --- below ones are for DDL parsing ---
    //


    /**
     * 解析参数称为创建表的SQL语句
     * @param dbName 创建表所在的数据库名
     * @param tableName 要创建的表名
     * @param jsonBody HTTP请求的请求体,json格式数据。数据格式为json列表:
     *                 <code>
     *                     [{"name":"column_name", "type":"column_type_string", "nullable":"true"}, {}, ...]
     *                 </code>
     *
     *                 <ul>
     *                     <li>
     *                         每一个json元素代表一个列信息;数据库中,列的顺序就是该列在这个json列表中的顺序;
     *                     </li>
     *                     <li>
     *                         <strong>name</strong>键的值为列名,建议以小写单词和下划线组合方式命名;
     *                     </li>
     *                     <li>
     *                         <strong>type</strong>键的值为列的数据类型字符串,如"INT"或"VARCHAR(n)"(可以参考MySQL列定义语法);
     *                     </li>
     *                     <li>
     *                         <strong>nullable</strong>元素可以省略,但如果存在且值为"true",则不标记该列"NOT NULL";
     *                     </li>
     *                     <li>
     *                         <strong>unique</strong>元素可以省略,但如果存在且值为"true",则标记该列是UNIQUE的;
     *                         unique元素诶true的情况下,index元素将被忽略;
     *                     </li>
     *                     <li>
     *                         <strong>index</strong>元素可以省略,但如果存在且值为"true",则为该列创建索引,索引名称为`index_{列名}`;
     *                     </li>
     *                     <li>
     *                         列信息中必须包含name和type元素,否则该列信息会被忽略;键名如"name","type"等,都为小写单词;
     *                     </li>
     *                 </ul>
     *
     *                 <strong>
     *                     注意:不用提供id列,系统默认会使用
     *                     <code>`id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY</code>
     *                     来创建主键。
     *                     这意味着即使传递一个空json列表,依然会创建一个表(只有默认的id列)。
     *                 </strong>
     * @param ifNotExist 是否在SQL中加上"IF NOT EXIST"子句,避免执行时碰到同名表而出现异常
     * @return 解析成的建表SQL语句
     * @throws InternalException 内部异常,用于异常信息传递
     */
    public String parseCreateTableSQL(String dbName, String tableName, String jsonBody, boolean ifNotExist)
            throws InternalException {

        ObjectMapper om = new ObjectMapper();
        List<Map<String, String>> rawBody;
        try {
            rawBody = om.readValue(jsonBody, List.class);
        }
        catch (IOException | ClassCastException ex) {
            throw new InternalException("parse json from request body failed: " + ex.getMessage());
        }

        List<ColumnInfo> columnInfoList = new LinkedList<>();
        rawBody.forEach(map -> {
            if (map.containsKey("name") && map.containsKey("type")) {
                String colName = map.get("name").toLowerCase();
                String colType = map.get("type").toUpperCase();
                boolean isNullable = map.containsKey("nullable") && map.get("nullable").equalsIgnoreCase("true");
                boolean isUnique = map.containsKey("unique") && map.get("unique").equalsIgnoreCase("true");
                boolean hasIndex = map.containsKey("index") && map.get("index").equalsIgnoreCase("true");

                String keys = "";
                if (isUnique) {
                    keys = "UNI";
                }
                else if (hasIndex) {
                    keys = "IND";
                }
                columnInfoList.add(new ColumnInfo(colName, colType, isNullable, keys));
            }
        });

        StringBuilder sb = new StringBuilder();
        if (ifNotExist) {
            sb.append("CREATE TABLE IF NOT EXISTS ");
        }
        else {
            sb.append("CREATE TABLE ");
        }
        sb.append(String.format("`%s`.`%s` (`id` INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,", dbName, tableName));

        columnInfoList.stream()
                .filter(col -> !col.getName().equalsIgnoreCase("id")) // ignore user-provided id column
                .forEachOrdered(
                        col -> {
                            sb.append(String.format("`%s` %s %s%s,",
                                        col.getName(),
                                        col.getColumnType(),
                                        col.isNullable()? "NULL" : "NOT NULL",
                                        col.getColumnKey().equalsIgnoreCase("UNI") ? " UNIQUE" : ""));

                            if (col.getColumnKey().equalsIgnoreCase("IND")) {
                                sb.append(String.format("INDEX `index_%s`(`%s`),", col.getName(), col.getName()));
                            }
                        });

        removeLastComma(sb, ");");
        return sb.toString();
    }

    /**
     * 解析建表SQL方法的重载,为参数IfNotExist提供默认参数:false
     * @param dbName 建表所在的数据库名
     * @param tableName 所建表名
     * @param jsonBody 建表所需的json参数
     * @return 建表SQL语句
     * @throws InternalException 内部异常,用以传递信息
     */
    public String parseCreateTableSQL(String dbName, String tableName, String jsonBody)
            throws InternalException {
        return parseCreateTableSQL(dbName, tableName, jsonBody, false);
    }

    /**
     * 解析参数构建drop表的SQL语句
     * @param dbName 数据库名
     * @param tableName 表名
     * @param ifExist 是否添加"IF EXISTS"标记
     * @return drop表的SQL语句
     */
    public String parseDropTableSQL(String dbName, String tableName, boolean ifExist) {
        if (ifExist) {
            return String.format("DROP TABLE IF EXISTS `%s`.`%s`", dbName, tableName);
        }
        else {
            return String.format("DROP TABLE `%s`.`%s`", dbName, tableName);
        }
    }

    /**
     * drop表解析方法的重载,为IfExist提供默认参数"false"
     * @param dbName 数据库名
     * @param tableName 表名
     * @return SQL语句
     */
    public String parseDropTableSQL(String dbName, String tableName) {
        return parseDropTableSQL(dbName, tableName, false);
    }
}
