package com.blueline.databus.core.bean;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.blueline.databus.core.config.DBConfig;
import com.blueline.databus.core.exception.InternalException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DBHelper {

    private DBConfig dbConfig;
    private SQLParser sqlParser;
    
    private Connection conn;

    @Autowired
    public DBHelper(DBConfig dbConfig, SQLParser sqlParser) {
        this.dbConfig = dbConfig;
        this.sqlParser = sqlParser;

        try {
            Class.forName(dbConfig.getDriverManager());
            conn = DriverManager.getConnection(
                    dbConfig.getUrl(),
                    dbConfig.getUsername(),
                    dbConfig.getPassword());
        }
        catch (ClassNotFoundException | SQLException ex) {
            System.err.print(ex.getMessage());
        }
    }

    private void close(Connection conn) {
        try {
            conn.close();
        }
        catch (SQLException ex) {
            System.err.print("Closing connection: " + ex.getMessage());
        }
    }

    /**
     * 获取数据
     * @param dbName
     * @param tableName
     * @param parameterMap
     * @return
     * @throws InternalException
     */
    public String getData(String dbName, String tableName, Map<String, String[]> parameterMap)
            throws InternalException {

        // 获取表的列信息
        List<ColumnInfo> columnsInfo = getColumns(dbName, tableName);
        if (columnsInfo == null || columnsInfo.size() < 1) {
            String err = String.format("getData:获取列信息(%s.%s)失败", dbName, tableName);
            System.err.print(err);
            throw new InternalException(err);
        }

        ObjectMapper om = new ObjectMapper();
        String result = null;
        List<Map<String, String>> dataResult = new ArrayList<>();

        // 拼凑完整的SQL语句;目前SELECT只支持获取所有字段
        String clauses = sqlParser.parseQueryString4Select(parameterMap);
        String sql = String.format("SELECT * FROM `%s`.`%s` %s", dbName, tableName, clauses);
        System.out.print("getData:拼凑的SQL语句为:" + sql);

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, String> lineResult = new HashMap<>();
                for (ColumnInfo col : columnsInfo) {
                    // 目前支持5类数据类型
                    if (col.getType().contains("int")) {
                        lineResult.put(col.getName(), String.valueOf(rs.getInt(col.getPosition())));
                    }
                    else if (col.getType() == "double") {
                        lineResult.put(col.getName(), String.valueOf(rs.getDouble(col.getPosition())));
                    }
                    else if (col.getType() == "float") {
                        lineResult.put(col.getName(), String.valueOf(rs.getFloat(col.getPosition())));
                    }
                    else if (col.getType().contains("char") || col.getType().contains("text")) {
                        lineResult.put(col.getName(), rs.getString(col.getPosition()));
                    }
                    else if (col.getType().contains("date")) {
                        lineResult.put(col.getName(), String.valueOf(rs.getDate(col.getPosition())));
                    }
                    dataResult.add(lineResult);
                }
            }
            result = om.writeValueAsString(dataResult);
        }
        catch (SQLException | JsonProcessingException ex) {
            System.err.print(ex.getMessage() + " | [SQL] " + sql + " [Json] " + result);
            throw new InternalException(ex.getMessage() + " <SQL> " + sql + " [Json] " + result);
        }
        finally {
            close(conn);
        }
        return result;
    }

    /**
     * 获取某表的所有列的名称、类型、位置
     * @param dbName 数据库名
     * @param tableName 表名
     * @return 列信息对象的列表 List<ColumnInfo>
     */
    private List<ColumnInfo> getColumns(String dbName, String tableName) {
        List<ColumnInfo> result = new ArrayList<>();
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
            System.err.print(ex.getMessage());
        }
        finally {
            close(conn);
        }
        return result;
    }

    /**
     * 创建数据库
     * @param dbName
     * @return
     */
    public int createDb(String dbName) throws InternalException {
        int count = 0;
        String sql = String.format("CREATE DATABASE `%s` DEFAULT CHARSET=utf8", dbName);

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            count = ps.executeUpdate();
        }
        catch (SQLException ex) {
            System.err.print(ex.getMessage());
            throw new InternalException("创建DB失败:" + ex.getMessage());
        }
        finally {
            close(conn);
        }
        return count;
    }

    /**
     * 创建数据表
     * @param dbName
     * @param query
     * @return
     */
    public int createDbTable(String dbName, String query){
        int count = 0;

        try {
            PreparedStatement ps = conn.prepareStatement(query);
            ps.executeUpdate();
            conn.commit();
            count = 1;
        } catch (Exception ex) {
            System.err.print(ex.getMessage());
            count = -1;
        }
        finally {
            close(conn);
        }
        return count;
    }

    /*
    // 插入数据（需要知道在那个数据库的那个表）
    public int insertData(String dbName, String tableName, String body) throws Exception{
        int count = 0;
        StringBuilder sb = new StringBuilder();
        
        try {
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            Map<List<String>, List<String>> map = ParamHelper.insertData(body);
            for (List<String> key : map.keySet()) {
                List<String> valueList = map.get(key);
                for (int i = 0; i < key.size(); i++) {
                    sql = "insert into " + tableName + " ( " + key.get(i) + " ) values ( "
                            + valueList.get(i) + " ) ";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    count = ps.executeUpdate() + count;
                }
            }
            conn.commit();
        }
        catch (SQLException ex) {
            conn.rollback();
            logger.error(ex.getMessage());
            throw new InternalException(ex.getMessage());
        }
        finally {
            close(conn);
        }
        return count;
    }
    */


    //修改数据
    public int updateData(String dbName, String tableName, String body) throws SQLException {
        int count = 0;
        try {
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            String sqlQuery = ParamHelper.updateData(body);
            String sql = " update " + tableName + " set " + sqlQuery + " ";
            PreparedStatement ps = conn.prepareStatement(sql);
            count = ps.executeUpdate();
            if(count>0){
                conn.commit();
            }else{
                conn.rollback();
            }
        }
        catch (Exception ex) {
            conn.rollback();
            System.err.print( ex.getMessage());
            ex.printStackTrace();
        }
        finally {
            close(conn);
        }
        return count;
    }
}
