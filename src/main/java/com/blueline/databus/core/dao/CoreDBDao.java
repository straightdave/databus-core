package com.blueline.databus.core.dao;

import com.blueline.databus.core.datatype.ColumnInfo;
import com.blueline.databus.core.exception.InternalException;
import com.blueline.databus.core.helper.SQLParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class CoreDBDao {
    private static final Logger logger = Logger.getLogger(CoreDBDao.class);
    private JdbcTemplate jdbcTemplate;
    private SQLParser sqlParser;

    @Autowired
    public void init(DataSource dataSource, SQLParser sqlParser) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.sqlParser = sqlParser;
    }

    /**
     * 创建表
     * @param dbName 数据库名
     * @param tableName 表名
     * @param jsonBody json参数
     */
    public void createTable(String dbName, String tableName, String jsonBody) {
        String sql = sqlParser.parseCreateTableSQL(dbName, tableName, jsonBody);
        logger.debug("createTable:拼凑的SQL语句为: " + sql);
        this.jdbcTemplate.execute(sql);
    }

    /**
     * 删除表
     * @param dbName 数据库名
     * @param tableName 表名
     * @return int 受影响的行数
     */
    public int dropTable(String dbName, String tableName) {
        String sql = sqlParser.parseDropTableSQL(dbName, tableName);
        logger.debug("dropTable:拼凑的SQL语句为: " + sql);
        return this.jdbcTemplate.update(sql);
    }

    /**
     * 从总线中读取数据
     * @param dbName 数据库名
     * @param tableName 表名
     * @param parameterMap 请求的参数键值对列表
     * @return 查询的数据转化成的JSON字符串
     */
    public String queryData(String dbName, String tableName, Map<String, String[]> parameterMap)
            throws InternalException, JsonProcessingException {

        final List<ColumnInfo> columnsInfo = getColumns(dbName, tableName);

        ObjectMapper om = new ObjectMapper();
        List<Map<String, String>> dataResult = new ArrayList<>();

        // 拼凑完整的SQL语句;目前SELECT只支持获取所有字段
        String clauses = sqlParser.parseSQL4Select(parameterMap);
        String sql = String.format("SELECT * FROM `%s`.`%s` %s", dbName, tableName, clauses);
        logger.debug("queryData:拼凑的SQL语句为: " + sql);

        SqlRowSet rs = this.jdbcTemplate.queryForRowSet(sql);
        while (rs.next()) {
            Map<String, String> lineResult = new HashMap<>();
            for (ColumnInfo col : columnsInfo) {
                // 目前支持获取5类数据类型进行填充
                if (col.getDataType().contains("int")) {
                    lineResult.put(col.getName(), String.valueOf(rs.getInt(col.getPosition())));
                }
                else if (col.getDataType().equalsIgnoreCase("double")) {
                    lineResult.put(col.getName(), String.valueOf(rs.getDouble(col.getPosition())));
                }
                else if (col.getDataType().equalsIgnoreCase("float")) {
                    lineResult.put(col.getName(), String.valueOf(rs.getFloat(col.getPosition())));
                }
                else if (col.getDataType().equalsIgnoreCase("varchar") ||
                        col.getDataType().equalsIgnoreCase("text")) {
                    lineResult.put(col.getName(), rs.getString(col.getPosition()));
                }
                else if (col.getDataType().equalsIgnoreCase("date") ||
                        col.getDataType().equalsIgnoreCase("datetime")) {
                    lineResult.put(col.getName(), String.valueOf(rs.getDate(col.getPosition())));
                }
                dataResult.add(lineResult);
            }
        }
        return om.writeValueAsString(dataResult);
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
        return this.jdbcTemplate.update(sql);
    }

    /**
     * 更新数据
     * @param dbName 数据库名
     * @param tableName 数据表名
     * @param colName 列名
     * @param colValue 列的值(作为条件)
     * @param paramMap 请求body中的参数键值对
     * @return 影响的行数
     * @throws InternalException 内部异常信息
     */
    public int updateData(String dbName, String tableName, String colName, String colValue, Map<String, String[]> paramMap)
            throws InternalException {
        String sql_format = sqlParser.parseSQL4Update(paramMap, colName, colValue);
        String sql = String.format(sql_format, dbName, tableName);
        logger.debug("updateData:拼凑的SQL语句为: " + sql);
        return this.jdbcTemplate.update(sql);
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
        return this.jdbcTemplate.update(sql);
    }

    /**
     * 获取某表的所有列的名称、类型、位置
     * dao内部使用,不抛出异常
     * @param dbName 数据库名
     * @param tableName 表名
     * @return 列信息对象的列表 List<ColumnInfo>
     */
    private List<ColumnInfo> getColumns(String dbName, String tableName)
            throws InternalException {
        String sql =
                "SELECT " +
                "COLUMN_NAME, DATA_TYPE, ORDINAL_POSITION, IS_NULLABLE, COLUMN_TYPE, COLUMN_KEY " +
                "FROM information_schema.COLUMNS " +
                "WHERE table_name = ? AND table_schema = ?";

        List<ColumnInfo> result = this.jdbcTemplate.query(
            sql,
            new Object[]{ tableName, dbName },
            new RowMapper<ColumnInfo>() {
                public ColumnInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return new ColumnInfo(
                        rs.getString("COLUMN_NAME").toLowerCase(),
                        rs.getString("DATA_TYPE").toLowerCase(),
                        rs.getInt("ORDINAL_POSITION"),
                        rs.getBoolean("IS_NULLABLE"),
                        rs.getString("COLUMN_TYPE"),
                        rs.getString("COLUMN_KEY")
                    );
                }
            }
        );

        if (result == null || result.size() < 1) {
            throw new InternalException(
                String.format("Get column info from `%s`.`%s` failed", dbName, tableName));
        }
        return result;
    }
}
