package com.blueline.databus.core.helper;

import java.io.IOException;
import java.sql.*;
import java.util.*;

import com.blueline.databus.core.bean.ColumnInfo;
import com.blueline.databus.core.config.DBConfig;
import com.blueline.databus.core.exception.InternalException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 负责对总线中客户数据的DML操作
 */
@Component
public class DBHelper {
    private static final Logger logger = Logger.getLogger(DBHelper.class);

    private final SQLParser sqlParser;

    private final DBConfig dbConfig;
    
    private Connection conn;

    @Autowired
    public DBHelper(DBConfig dbConfig, SQLParser sqlParser)
            throws InternalException {
        this.dbConfig = dbConfig;
        this.sqlParser = sqlParser;

        try {
            Class.forName(this.dbConfig.getDriverManager());
            conn = DriverManager.getConnection(
                    this.dbConfig.getUrl(),
                    this.dbConfig.getUsername(),
                    this.dbConfig.getPassword());
        }
        catch (ClassNotFoundException | SQLException ex) {
            String err = String.format("DBHelper init failed: %s", ex.getMessage());
            logger.error(err);
            throw new InternalException(err);
        }
    }

    /**
     * 从总线中读取数据
     * @param dbName 数据库名
     * @param tableName 表名
     * @param parameterMap 请求的参数键值对列表
     * @return 查询的数据转化成的JSON字符串
     * @throws InternalException 内部异常
     */
    public String queryData(String dbName, String tableName, Map<String, String[]> parameterMap)
            throws InternalException {
        // 获取表的列信息
        final List<ColumnInfo> columnsInfo = getColumns(dbName, tableName);
        if (columnsInfo == null || columnsInfo.size() < 1) {
            String err = String.format("queryData:获取列信息(%s.%s)失败", dbName, tableName);
            logger.error(err);
            throw new InternalException(err);
        }

        ObjectMapper om = new ObjectMapper();
        List<Map<String, String>> dataResult = new ArrayList<>();

        // 拼凑完整的SQL语句;目前SELECT只支持获取'*'字段
        String clauses = sqlParser.parseSQL4Select(parameterMap);
        String sql = String.format("SELECT * FROM `%s`.`%s` %s", dbName, tableName, clauses);
        logger.info("queryData:拼凑的SQL语句为: " + sql);

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, String> lineResult = new HashMap<>();
                for (ColumnInfo col : columnsInfo) {
                    // 目前支持获取5类数据类型进行填充
                    if (col.getType().contains("int")) {
                        lineResult.put(col.getName(), String.valueOf(rs.getInt(col.getPosition())));
                    }
                    else if (col.getType() == "double") {
                        lineResult.put(col.getName(), String.valueOf(rs.getDouble(col.getPosition())));
                    }
                    else if (col.getType() == "float") {
                        lineResult.put(col.getName(), String.valueOf(rs.getFloat(col.getPosition())));
                    }
                    else if (col.getType().contains("char") ||
                            col.getType().contains("text")) {
                        lineResult.put(col.getName(), rs.getString(col.getPosition()));
                    }
                    else if (col.getType().contains("date")) {
                        lineResult.put(col.getName(), String.valueOf(rs.getDate(col.getPosition())));
                    }
                    dataResult.add(lineResult);
                }
            }
            return om.writeValueAsString(dataResult);
        }
        catch (SQLException | JsonProcessingException ex) {
            String err = String.format("DBHelper:queryData failed: %s | [SQL] %s",
                    ex.getMessage(), sql);
            logger.error(err);
            throw new InternalException(err);
        }
    }

    /**
     * 插入数据
     * @param dbName 数据库名
     * @param tableName 数据表名
     * @param jsonBody 请求body中的json
     * @return 影响的行数
     * @throws InternalException 内部异常
     */
    public int insertData(String dbName, String tableName, String jsonBody)
            throws InternalException {

        // 获取表的列信息
        List<ColumnInfo> columnsInfo = getColumns(dbName, tableName);
        if (columnsInfo == null || columnsInfo.size() < 1) {
            String err = String.format("insertData:获取列信息(%s.%s)失败", dbName, tableName);
            logger.error(err);
            throw new InternalException(err);
        }

        String sql_format, sql;
        try {
            sql_format = sqlParser.parseSQL4Insert(jsonBody, columnsInfo);
            sql = String.format(sql_format, dbName, tableName); // 为sql_format加上表名
            logger.info("insertData:拼凑的SQL语句为: " + sql);
        }
        catch (IOException ex) {
            logger.error("DBHelper: insertData: " + ex.getMessage());
            throw new InternalException("insertData failed: " + ex.getMessage());
        }

        int count;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            count = ps.executeUpdate();
            logger.info(String.format("%s rows inserted by: %s", count, sql));
        }
        catch (SQLException ex) {
            logger.error("DBHelper: insertData: " + ex.getMessage());
            throw new InternalException("insertData failed: " + ex.getMessage());
        }
        return count;
    }

    /**
     * 更新数据
     * @param dbName 数据库名
     * @param tableName 数据表名
     * @param colName 列名
     * @param colValue 列的值(作为条件)
     * @param paramMap 请求body中的参数键值对
     * @return 影响的行数
     * @throws InternalException
     */
    public int updateData(String dbName, String tableName, String colName, String colValue, Map<String, String[]> paramMap)
            throws InternalException {

        String sql_format = sqlParser.parseSQL4Update(paramMap, colName, colValue);
        String sql = String.format(sql_format, dbName, tableName); // 为sql_format加上表名
        logger.info("updateData:拼凑的SQL语句为: " + sql);

        int count;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            count = ps.executeUpdate();
            logger.info(String.format("%s rows undated by: %s", count, sql));
        }
        catch (SQLException ex) {
            logger.error("DBHelper: updateData: " + ex.getMessage());
            throw new InternalException("updateData failed: " + ex.getMessage());
        }
        return count;
    }

    /**
     * 删除数据
     * @param dbName 数据库名
     * @param tableName 数据表名
     * @param parameterMap 请求body中的参数键值对
     * @return 影响的行数
     * @throws InternalException 内部异常
     */
    public int deleteData(String dbName, String tableName, Map<String, String[]> parameterMap)
            throws InternalException {

        String clauses = sqlParser.parseSQL4Delete(parameterMap);
        String sql = String.format("DELETE FROM `%s`.`%s` %s", dbName, tableName, clauses);
        logger.info("deleteData:拼凑的SQL语句为: " + sql);

        int count;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            count = ps.executeUpdate();
            logger.info(String.format("%s rows deleted by: %s", count, sql));
        }
        catch (SQLException ex) {
            logger.error("DBHelper: deleteData: " + ex.getMessage());
            throw new InternalException("deleteData failed: " + ex.getMessage());
        }
        return count;
    }

    /**
     * 获取某表的所有列的名称、类型、位置
     * DBHelper内部使用,不抛出异常
     * @param dbName 数据库名
     * @param tableName 表名
     * @return 列信息对象的列表 List<ColumnInfo>
     */
    private List<ColumnInfo> getColumns(String dbName, String tableName) {
        List<ColumnInfo> result = new LinkedList<>();
        String sql = "SELECT COLUMN_NAME, DATA_TYPE, ORDINAL_POSITION FROM information_schema.COLUMNS WHERE table_name = ? AND table_schema = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, dbName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(new ColumnInfo(
                                rs.getString("COLUMN_NAME").toLowerCase(),
                                rs.getString("DATA_TYPE").toLowerCase(),
                                rs.getInt("ORDINAL_POSITION")));
            }
        }
        catch (SQLException ex) {
            logger.error("DBHelper:getColumns failed: " + ex.getMessage());
        }
        return result;
    }

    public void createTable(String dbName, String jsonBody)
            throws InternalException {

        String sql = sqlParser.parseSQL4CreateTable(dbName, jsonBody);
        logger.info("DBHelper:CreateTable [SQL] " + sql);

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
            logger.info("DBHelper:CreateTable done");
        } catch (Exception ex) {
            String err = String.format("DBHelper:CreateTable failed: %s | [SQL] %s", ex.getMessage(), sql);
            logger.error(err);
            throw new InternalException(err);
        }
    }


}
