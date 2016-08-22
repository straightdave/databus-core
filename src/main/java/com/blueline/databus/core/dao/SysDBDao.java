package com.blueline.databus.core.dao;

import com.blueline.databus.core.datatype.AclInfo;
import com.blueline.databus.core.datatype.Client;
import com.blueline.databus.core.helper.RandomStringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.sql.*;
import java.util.*;

@Repository
public class SysDBDao {

    @Autowired
    @Qualifier("templateSys")
    private JdbcTemplate templateSys;

    /**
     * 通过appkey从DB里面拿skey
     * @param appKey client的app key
     * @return skey client的secure key
     */
    public String getSKey(String appKey) {
        return this.templateSys.queryForObject(
                "SELECT `skey` FROM `clients` WHERE `appkey` = ?",
                String.class, appKey);
    }

    /**
     * 返回所有Acl数据表数据
     * 只在同package的AclLoader类中使用
     * @return AclInfo的数组
     */
    public List<AclInfo> getAllAclInfo() {
        return this.templateSys.query(
            "SELECT `api`, `method`, `appkey`, `duration` FROM `databus_sys`.`acl`",
            new RowMapper<AclInfo>() {
                public AclInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return new AclInfo(
                        rs.getString("api"),
                        rs.getString("method"),
                        rs.getString("appkey"),
                        rs.getString("duration")
                    );
                }
            }
        );
    }

    public AclInfo getAclInfoByApi(String api) {
        return this.templateSys.queryForObject(
            "SELECT `api`, `method`, `appkey`, `duration` FROM `databus_sys`.`acl` WHERE `api` = ?",
            new Object[] { api },
            new RowMapper<AclInfo>() {
                public AclInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return new AclInfo(
                        rs.getString("api"),
                        rs.getString("method"),
                        rs.getString("appkey"),
                        rs.getString("duration")
                    );
                }
            }
        );
    }

    public AclInfo checkAclInfo(String api, String method, String appKey) {
        System.out.println("==> check acl from sys db: " + api);

        AclInfo result;
        try {
            result = this.templateSys.queryForObject(
                    "SELECT `api`, `method`, `appkey`, `duration` FROM `databus_sys`.`acl` WHERE `api` = ? AND `method` = ? AND `appkey` = ?",
                    new Object[]{api, method, appKey},
                    new RowMapper<AclInfo>() {
                        public AclInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                            return new AclInfo(
                                    rs.getString("api"),
                                    rs.getString("method"),
                                    rs.getString("appkey"),
                                    rs.getString("duration")
                            );
                        }
                    }
            );
        }
        catch (Exception ex) {
            // 不向上层抛出异常,上层用返回值区别结果
            System.err.println("==> check acl failed: " + ex.getMessage());
            return null;
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

    public int refreshKeys(int clientId) {
        String newAppKey = RandomStringHelper.getRandomString(10);
        String newSKey = RandomStringHelper.hashKey(newAppKey);

        String sql = String.format("UPDATE `databus_sys`.`clients` SET `appkey` = '%s', `skey` = '%s' WHERE `id` = '%s'",
                newAppKey, newSKey, clientId);
        return this.templateSys.update(sql);
    }

    public int refreshKeys(String name) {
        String newAppKey = RandomStringHelper.getRandomString(10);
        String newSKey = RandomStringHelper.hashKey(newAppKey);

        String sql = String.format("UPDATE `databus_sys`.`clients` SET `appkey` = '%s', `skey` = '%s' WHERE `name` = '%s'",
                newAppKey, newSKey, name);
        return this.templateSys.update(sql);
    }

}
