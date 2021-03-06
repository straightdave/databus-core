package com.blueline.databus.core.dao;

import com.blueline.databus.core.datatype.ColumnInfo;
import com.blueline.databus.core.exception.InternalException;
import com.blueline.databus.core.helper.SQLParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.*;

@Repository
public class CoreDBDao {
    private static final Logger logger = Logger.getLogger(CoreDBDao.class);

    @Autowired
    private JdbcTemplate templateCore;

    @Autowired
    private SQLParser sqlParser;

    /**
     * 创建表
     * @param dbName 数据库名
     * @param tableName 表名
     * @param columns 解析自json格式参数，元素为列信息
     * @throws InternalException 内部异常
     */
    public void createTable(String dbName, String tableName, ArrayList<Map<String, Object>> columns)
            throws InternalException {
        String sql = "";
        try {
            sql = sqlParser.parseCreateTableSQL(dbName, tableName, columns);
            logger.debug("createTable:拼凑的SQL语句为: " + sql);
            System.out.println("sql => " + sql);
            this.templateCore.execute(sql);
        }
        catch (Exception ex) {
            throw new InternalException("sql:" + sql + "; " + ex.getMessage());
        }
    }

    /**
     * 创建表(如果表已存在不会抛出异常,主要用于测试目的)
     * @param dbName 数据库名
     * @param tableName 表名
     * @param columns ArrayList<HashMap> 解析自json格式参数，元素为列信息
     * @throws InternalException 内部异常
     */
    public void createTableIfNotExist(String dbName, String tableName, ArrayList<Map<String, Object>> columns)
            throws InternalException {
        String sql = "<null>";
        try {
            sql = sqlParser.parseCreateTableSQL(dbName, tableName, columns, true);
            logger.debug("createTable:拼凑的SQL语句为: " + sql);
            this.templateCore.execute(sql);
        }
        catch (Exception ex) {
            throw new InternalException("sql:" + sql + "; " + ex.getMessage());
        }
    }

    /**
     * 删除表
     * @param dbName 数据库名
     * @param tableName 表名
     * @return 受影响的行数
     */
    public int dropTable(String dbName, String tableName) {
        String sql = sqlParser.parseDropTableSQL(dbName, tableName);
        logger.debug("dropTable:拼凑的SQL语句为: " + sql);
        return this.templateCore.update(sql);
    }

    /**
     * 删除表(如果表不存在也不会抛出异常,主要用于测试目的)
     * @param dbName 数据库名
     * @param tableName 表名
     * @return 受影响的行数
     */
    public int dropTableIfExist(String dbName, String tableName) {
        String sql = sqlParser.parseDropTableSQL(dbName, tableName, true);
        logger.debug("dropTable:拼凑的SQL语句为: " + sql);
        return this.templateCore.update(sql);
    }

    /**
     * 从总线数据库的表中读取数据
     * 目前只支持读取所有字段
     * @param dbName 数据库名
     * @param tableName 表名
     * @param parameterMap 请求的参数键值对列表
     * @return 查询的数据结果转化成的JSON字符串
     * @throws InternalException 内部异常
     * @throws JsonProcessingException Json处理异常
     */
    public String queryData(String dbName, String tableName, Map<String, String[]> parameterMap)
            throws InternalException, JsonProcessingException {
        // 拼凑完整的SQL语句;目前SELECT只支持获取所有字段
        String clauses = sqlParser.parseSQL4Select(parameterMap);
        String sql = String.format("SELECT * FROM `%s`.`%s` %s", dbName, tableName, clauses);
        logger.debug("queryData:拼凑的SQL语句为: " + sql);

        List<Map<String, Object>> result = this.templateCore.queryForList(sql);
        if (result.size() < 1) {
            // build bare json structure and return
            List<ColumnInfo> cols = getColumns(dbName, tableName);

            Map<String, Object> innerMap = new HashMap<>();
            cols.forEach(col -> innerMap.put(col.getName(), "") );
            result.add(innerMap);
        }

        ObjectMapper om = new ObjectMapper();
        return om.writeValueAsString(result);
    }

    /**
     * 插入数据
     * @param dbName 数据库名
     * @param tableName 数据表名
     * @param jsonBody 请求body中的json
     * @return 影响的行数
     * @throws InternalException 内部异常信息
     */
    public int insertData(String dbName, String tableName, String jsonBody)
            throws InternalException {
        final List<ColumnInfo> columnsInfo = getColumns(dbName, tableName);
        String sql_format = sqlParser.parseSQL4Insert(jsonBody, columnsInfo);
        String sql = String.format(sql_format, dbName, tableName);
        logger.debug("insertData:拼凑的SQL语句为: " + sql);
        return this.templateCore.update(sql);
    }

    /**
     * 更新数据
     * @param dbName 数据库名
     * @param tableName 数据表名
     * @param colName 列名
     * @param colValue 列的值(作为条件)
     * @param jsonBody json参数
     * @return 影响的行数
     * @throws InternalException 内部异常信息
     */
    public int updateData(String dbName, String tableName, String colName, String colValue, String jsonBody)
            throws InternalException {
        String sql_format = sqlParser.parseSQL4Update(colName, colValue, jsonBody);
        String sql = String.format(sql_format, dbName, tableName);
        System.out.println(sql);
        logger.debug("updateData:拼凑的SQL语句为: " + sql);
        return this.templateCore.update(sql);
    }

    /**
     * 删除数据
     * @param dbName 数据库名
     * @param tableName 数据表名
     * @param parameterMap 请求body中的参数键值对
     * @return 影响的行数
     * @throws InternalException 内部异常信息
     */
    public int deleteData(String dbName, String tableName, Map<String, String[]> parameterMap)
            throws InternalException {
        String clauses = sqlParser.parseSQL4Delete(parameterMap);
        String sql = String.format("DELETE FROM `%s`.`%s` %s", dbName, tableName, clauses);
        logger.debug("deleteData:拼凑的SQL语句为: " + sql);
        return this.templateCore.update(sql);
    }

    /**
     * 获取某表的所有列的名称、类型、位置
     * <strong>MySQL的实现</strong>
     * dao内部使用,不抛出异常
     * @param dbName 数据库名
     * @param tableName 表名
     * @return 列信息对象的列表
     */
    public List<ColumnInfo> getColumns(String dbName, String tableName)
            throws InternalException {
        List<ColumnInfo> result = this.templateCore.query(
                "SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, ORDINAL_POSITION, COLUMN_COMMENT, IS_NULLABLE, COLUMN_TYPE, COLUMN_KEY " +
                "FROM information_schema.COLUMNS " +
                "WHERE table_schema = ? AND table_name = ?",
                new Object[] {dbName, tableName},
                (ResultSet rs, int rowNum) -> new ColumnInfo(
                        rs.getString("COLUMN_NAME").toLowerCase(),
                        rs.getString("DATA_TYPE").toUpperCase(), // column's short data type (no precision or length)
                        rs.getInt("CHARACTER_MAXIMUM_LENGTH"),
                        rs.getInt("ORDINAL_POSITION"),
                        rs.getString("COLUMN_COMMENT"),
                        rs.getBoolean("IS_NULLABLE"),
                        rs.getString("COLUMN_TYPE"). toUpperCase(), // full-text of column's data type
                        rs.getString("COLUMN_KEY").toUpperCase()
                )
        );

        if (result == null || result.size() < 1) {
            throw new InternalException(
                String.format("Get column info from `%s`.`%s` failed", dbName, tableName));
        }
        return result;
    }
}
