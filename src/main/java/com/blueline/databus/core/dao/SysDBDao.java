package com.blueline.databus.core.dao;

import com.blueline.databus.core.datatype.AclInfo;
import com.blueline.databus.core.exception.InternalException;
import com.blueline.databus.core.helper.RandomStringHelper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.*;

@Repository
public class SysDBDao {
    private static final Logger logger = Logger.getLogger(SysDBDao.class);

    @Autowired
    @Qualifier("templateSys")
    private JdbcTemplate templateSys;

    /**
     * 通过appkey获取用户的id
     * @param appKey 用户的appkey
     * @return 用户的id
     */
    public int getClientID(String appKey) {
        try {
            return this.templateSys.queryForObject(
                    "SELECT `id` FROM `databus_sys`.`clients` WHERE `appkey` = ?",
                    Integer.class, appKey);
        }
        catch (DataAccessException ex) {
            logger.info("got no client id for appkey: " + appKey + ": " + ex.getMessage());
            return -1;
        }
    }

    /**
     * 通过appkey获取用户的skey
     * @param appKey client的app key
     * @return 用户的skey
     */
    public String getSKey(String appKey) {
        return this.templateSys.queryForObject(
                "SELECT `skey` FROM `databus_sys`.`clients` WHERE `appkey` = ?",
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

    /**
     * 根据api路径获取acl信息对象
     * @param api api路径
     * @return acl信息
     */
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
            System.err.println("==> got no acl from DB: " + ex.getMessage());
            return null;
        }
        return result;
    }


    /**
     * 建表成功之后,需要为表增加表数据、增删改查(DML)的接口(API)数据
     * @param dbName 数据库名
     * @param tableName 表名
     * @param ownerId 建表人的id
     * @throws InternalException 内部异常
     */
    public void doAfterTableCreated(String dbName, String tableName, int ownerId)
            throws InternalException {

        String sql_add_table =
                "INSERT INTO `tables` (`name`, `db_name`, `owner_id`, `created_at`) VALUES (?, ?, ?, now())";
        String sql_add_interface =
                "INSERT INTO `interfaces` (`table_name`, `db_name`, `api`, `method`, `created_at`) VALUES (?, ?, ?, ?, now())";

        Connection conn = null;
        try {
            conn = this.templateSys.getDataSource().getConnection();
            conn.setAutoCommit(false);

            // add new entry to table `tables`
            PreparedStatement ps = conn.prepareStatement(sql_add_table);
            ps.setString(1, tableName);
            ps.setString(2, dbName);
            ps.setInt(3, ownerId);
            ps.executeUpdate();

            // add 'insert' api to table `interfaces`
            PreparedStatement ps1 = conn.prepareStatement(sql_add_interface);
            ps1.setString(1, tableName);
            ps1.setString(2, dbName);
            ps1.setString(3, "/api/data/" + dbName + "/" + tableName);
            ps1.setString(4, "POST");
            ps1.executeUpdate();

            // add 'delete' api to table `interfaces`
            PreparedStatement ps2 = conn.prepareStatement(sql_add_interface);
            ps2.setString(1, tableName);
            ps2.setString(2, dbName);
            ps2.setString(3, "/api/data/" + dbName + "/" + tableName);
            ps2.setString(4, "DELETE");
            ps2.executeUpdate();

            // add 'update' api to table `interfaces`
            PreparedStatement ps3 = conn.prepareStatement(sql_add_interface);
            ps3.setString(1, tableName);
            ps3.setString(2, dbName);
            ps3.setString(3, "/api/data/" + dbName + "/" + tableName + "/**");
            ps3.setString(4, "PUT");
            ps3.executeUpdate();

            // add 'select' api to table `interfaces`
            PreparedStatement ps4 = conn.prepareStatement(sql_add_interface);
            ps4.setString(1, tableName);
            ps4.setString(2, dbName);
            ps4.setString(3, "/api/data/" + dbName + "/" + tableName);
            ps4.setString(4, "GET");
            ps4.executeUpdate();

            conn.commit();
        }
        catch (SQLException ex) {
            System.out.println("post-creation failed: going to rollback...: " + ex.getMessage());
            logger.fatal("transaction failed. rollback. " + ex.getMessage());
            try {
                if (conn != null) {
                    conn.rollback();
                }
                System.out.println("post-creation rollback done.");
            }
            catch (SQLException sql_ex) {
                // eat it
                logger.info("post-creation: rollback transaction failed: " + ex.getMessage());
            }
            throw new InternalException("post-creation action failed: " + ex.getMessage());
        }
        finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            }
            catch (SQLException ex) {
                // eat this
                logger.info("reset auto-commit to true failed: " + ex.getMessage());
            }
        }
    }

    /**
     * 删除表后,需要将该表数据、相关的接口数据清除
     * @param dbName 数据库名
     * @param tableName 表名
     * @throws InternalException 内部异常
     */
    public void doAfterTableDropped(String dbName, String tableName)
            throws InternalException {

        String sql_delete_table =
                "DELETE FROM `tables` WHERE `db_name` = ? AND `name` = ?";
        String sql_delete_interface =
                "DELETE FROM `interfaces` WHERE `db_name` = ? AND `table_name` = ?";

        Connection conn = null;
        try {
            conn = this.templateSys.getDataSource().getConnection();
            conn.setAutoCommit(false);

            PreparedStatement ps = conn.prepareStatement(sql_delete_table);
            ps.setString(1, dbName);
            ps.setString(2, tableName);
            ps.executeUpdate();

            PreparedStatement ps1 = conn.prepareStatement(sql_delete_interface);
            ps1.setString(1, dbName);
            ps1.setString(2, tableName);
            ps1.executeUpdate();

            conn.commit();
        }
        catch (SQLException ex) {
            logger.fatal("transaction failed. rollback.");
            try {
                if (conn != null) {
                    conn.rollback();
                }
            }
            catch (SQLException sql_ex) {
                // eat it
                logger.info("post-dropping: rollback transaction failed: " + ex.getMessage());
            }
            throw new InternalException("post-dropping action failed: " + ex.getMessage());
        }
        finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                }
            }
            catch (SQLException ex) {
                // eat this
                logger.info("reset auto-commit to true failed: " + ex.getMessage());
            }
        }
    }

    /**
     * 重置用户的appkey和skey
     * @param clientId 用户id
     * @return 受影响行数
     */
    public int refreshKeys(int clientId) {
        String newAppKey = RandomStringHelper.getRandomString(10);
        String newSKey = RandomStringHelper.hashKey(newAppKey);
        String sql = String.format(
                "UPDATE `databus_sys`.`clients` SET `appkey` = '%s', `skey` = '%s' WHERE `id` = '%s'",
                newAppKey, newSKey, clientId);
        return this.templateSys.update(sql);
    }

    /**
     * 重置用户的appkey和skey
     * @param name 用户名
     * @return 受影响行数
     */
    public int refreshKeys(String name) {
        String newAppKey = RandomStringHelper.getRandomString(10);
        String newSKey = RandomStringHelper.hashKey(newAppKey);
        String sql = String.format(
                "UPDATE `databus_sys`.`clients` SET `appkey` = '%s', `skey` = '%s' WHERE `name` = '%s'",
                newAppKey, newSKey, name);
        return this.templateSys.update(sql);
    }
}
