package com.blueline.databus.core.dao;

import com.blueline.databus.core.datatype.AclInfo;
import com.blueline.databus.core.configtype.SysDBConfig;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

/**
 * databus系统信息db处理helper
 */
@Component
public class SysDBDao {
    private static final Logger logger = Logger.getLogger(SysDBDao.class);



    private Connection conn = null;

    @Autowired
    private SysDBDao(SysDBConfig config) {
        sysDBConfig = config;

        try {
            Class.forName(sysDBConfig.getDriverManager());
            conn = DriverManager.getConnection(
                    sysDBConfig.getUrl(),
                    sysDBConfig.getUsername(),
                    sysDBConfig.getPassword());
        }
        catch (ClassNotFoundException | SQLException ex) {
            logger.error("init DB: " + ex.getMessage());
        }
    }

    /**
     * 通过appkey从DB里面拿skey
     * @param appKey client的app key
     * @return skey client的secure key
     */
    public String getSKey(String appKey) {
        String sql = String.format("SELECT skey FROM clients WHERE appkey='%s'", appKey);
        String result = null;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                result = rs.getString("skey");
                if (!StringUtils.isEmpty(result)) {
                    break; // 只获取第一个(理论上只应该有一个)
                }
            }
        }
        catch (SQLException ex) {
            logger.error("getSKey: " + ex.getMessage());
        }
        return result;
    }

    /**
     * 返回所有Acl数据表数据
     * 只在同package的AclLoader类中使用
     * @return AclInfo的数组
     */
    public List<AclInfo> getAclInfo() {
        String sql = "SELECT `id`, `api`, `method`, `appkey`, `duration` FROM `acl`";
        List<AclInfo> result = new LinkedList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                result.add(new AclInfo(
                                rs.getString("api"),
                                rs.getString("method"),
                                rs.getString("appkey"),
                                rs.getString("duration")));
            }
        }
        catch (SQLException ex) {
            logger.error("SysDBDao: getAclInfo: " + ex.getMessage());
        }
        return result;
    }


    /**
     * 建表成功之后,需要为表自动生成interfaces(API),且为owner加上这些权限
     * @param dbName
     * @param tableName
     * @param appKey
     * @return
     */
    public int doAfterTableCreated(String dbName, String tableName, String appKey) {

        return 1;
    }

    public int addInterface4NewTable(String dbName, String tableName) {




        return 1;
    }

}
