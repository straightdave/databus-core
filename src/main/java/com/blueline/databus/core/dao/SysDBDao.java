package com.blueline.databus.core.dao;

import com.blueline.databus.core.datatype.AclInfo;
import com.blueline.databus.core.datatype.ClientInfo;
import com.blueline.databus.core.datatype.InterfaceInfo;
import com.blueline.databus.core.datatype.TableInfo;
import com.blueline.databus.core.exception.InternalException;
import com.blueline.databus.core.helper.RandomStringHelper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.util.*;

@Repository
public class SysDBDao {
    private static final Logger logger = Logger.getLogger(SysDBDao.class);

    @Autowired
    @Qualifier("templateSys")
    private JdbcTemplate templateSys;

    /**
     * 根据client的id获取client信息
     * @param id client的id
     * @return ClientInfo实例
     */
    public ClientInfo getClientByID(int id) {
        return this.templateSys.queryForObject(
                "SELECT `id`,`name`,`display_name`,`description`,`status`,`appkey`,`skey`,`client_type`,`client_category` " +
                "FROM `clients` WHERE `id` = ?",
                new Object[]{id},
                (ResultSet rs, int i) -> new ClientInfo(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("display_name"),
                        rs.getString("description"),
                        rs.getInt("status"),
                        rs.getString("appkey"),
                        rs.getString("skey"),
                        rs.getString("client_type"),
                        rs.getString("client_category")
                )
        );
    }

    /**
     * 根据client的name(唯一、不变)获取client信息
     * @param name client的name
     * @return ClientInfo实例
     */
    public ClientInfo getClientByName(String name) {
        return this.templateSys.queryForObject(
                "SELECT `id`,`name`,`display_name`,`description`,`status`,`appkey`,`skey`,`client_type`,`client_category` " +
                "FROM `clients` WHERE `name` = ?",
                new Object[]{name},
                (ResultSet rs, int i) -> new ClientInfo(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("display_name"),
                        rs.getString("description"),
                        rs.getInt("status"),
                        rs.getString("appkey"),
                        rs.getString("skey"),
                        rs.getString("client_type"),
                        rs.getString("client_category")
                )
        );
    }

    /**
     * 根据client的appkey反向获取client信息
     * @param appKey client的appkey
     * @return ClientInfo实例
     */
    public ClientInfo getClientByAppKey(String appKey) {
        return this.templateSys.queryForObject(
                "SELECT `id`,`name`,`display_name`,`description`,`status`,`appkey`,`skey`,`client_type`,`client_category` " +
                        "FROM `clients` WHERE `appkey` = ?",
                new Object[]{appKey},
                (ResultSet rs, int i) -> new ClientInfo(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("display_name"),
                        rs.getString("description"),
                        rs.getInt("status"),
                        rs.getString("appkey"),
                        rs.getString("skey"),
                        rs.getString("client_type"),
                        rs.getString("client_category")
                )
        );
    }

    /**
     * 获取所有client信息
     * @return ClientInfo实例列表
     */
    public List<ClientInfo> getClients() {
        return this.templateSys.query(
                "SELECT `id`, `name`, `display_name`, `description`, `appkey`, `skey`, `client_type`, `client_category` " +
                "FROM `databus_sys`.`clients`",
                (ResultSet rs, int i) -> new ClientInfo(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("display_name"),
                        rs.getString("description"),
                        rs.getInt("status"),
                        rs.getString("appkey"),
                        rs.getString("skey"),
                        rs.getString("client_type"),
                        rs.getString("client_category")
                )
        );
    }


    /**
     * 创建新client
     * @param name client名(唯一、不变)
     * @param displayName 显示名称
     * @param description 描述
     * @param clientType 类型
     * @param clientCategory 范畴
     * @return 受影响行数
     */
    public int createClient(String name, String displayName, String description,
                            String clientType, String clientCategory
    ) {
        return this.templateSys.update(
                "INSERT INTO `clients`(`name`,`display_name`,`description`,`client_type`,`client_category`) " +
                "VALUES (?,?,?,?,?)",
                name, displayName, description, clientType, clientCategory
        );
    }

    /**
     * 通过client名将其状态置为'挂起(1)'; '0'为正常
     * @param name client名
     * @return 受影响行数
     */
    public int suspendClient(String name) {
        return this.templateSys.update("UPDATE `clients` SET `status` = 1 WHERE `name` = ?", name);
    }

    /**
     * 通过client名将其状态置为'正常(0)'
     * @param name client名
     * @return 受影响行数
     */
    public int resumeClient(String name) {
        return this.templateSys.update("UPDATE `clients` SET `status` = 0 WHERE `name` = ?", name);
    }

    /**
     * 重置client的appkey和skey
     * @param name client名
     * @return 受影响行数
     */
    public int refreshKeys(String name) {
        String newAppKey = RandomStringHelper.getRandomString(10);
        String newSKey = RandomStringHelper.hashKey(newAppKey);
        return this.templateSys.update(
                "UPDATE `clients` SET `appkey` = ?, `skey` = ? WHERE `name` = ?",
                newAppKey, newSKey, name);
    }


    /**
     * 读取数据库acl表(记载interface和client的关系)所有记录
     * 将详细的acl信息填充到AclInfo实例列表
     * 用于程序启动时载入acl信息到缓存
     * @return AclInfo实例列表
     */
    public List<AclInfo> getAllAclInfo() {
        return this.templateSys.query(
            "SELECT `i`.`api` AS `api`, `i`.`method` AS `method`, `c`.`name` AS `client_name`, `a`.`duration` AS `duration` " +
            "FROM `acl` AS `a` INNER JOIN `interfaces` AS `i` ON `a`.`interface_id` = `i`.`id` INNER JOIN `clients` AS `c` ON `a`.`client_id` = `c`.`id`",
            (ResultSet rs, int rowNum) -> new AclInfo(
                    rs.getString("api"),
                    rs.getString("method"),
                    rs.getString("client_name"),
                    rs.getString("duration")
            )
        );
    }

    /**
     * 根据接口id获取acl信息对象
     * @param interfaceId 接口id
     * @return AclInfo实例
     */
    public List<AclInfo> getAclInfoByInterface(int interfaceId) {
        return this.templateSys.query(
            "SELECT `i`.`api` AS `api`, `i`.`method` AS `method`, `c`.`name` AS `client_name`, `a`.`duration` AS `duration` " +
            "FROM `acl` AS `a` INNER JOIN `interfaces` AS `i` ON `a`.`interface_id` = `i`.`id` INNER JOIN `clients` AS `c` ON `a`.`client_id` = `c`.`id` " +
            "WHERE `i`.`id` = ?",
            new Object[] {interfaceId},
            (ResultSet rs, int rowNum) -> new AclInfo(
                    rs.getString("api"),
                    rs.getString("method"),
                    rs.getString("client_name"),
                    rs.getString("duration")
            )
        );
    }

    /**
     * 根据用户name获取其acl记录列表
     * @param clientName client名
     * @return AclInfo实例列表
     */
    public List<AclInfo> getAclInfoByClient(String clientName) {
        return this.templateSys.query(
                "SELECT `i`.`api` AS `api`, `i`.`method` AS `method`, `c`.`name` AS `client_name`, `a`.`duration` AS `duration` " +
                "FROM `acl` AS `a` INNER JOIN `interfaces` AS `i` ON `a`.`interface_id` = `i`.`id` INNER JOIN `clients` AS `c` ON `a`.`client_id` = `c`.`id` " +
                "WHERE `c`.`name` = ?",
                new Object[] {clientName},
                (ResultSet rs, int rowNum) -> new AclInfo(
                        rs.getString("api"),
                        rs.getString("method"),
                        rs.getString("client_name"),
                        rs.getString("duration")
                )
        );
    }

    /**
     * 根据条件获取acl信息
     * @param api api路径
     * @param method http方法
     * @param clientName 访问者name
     * @return AclInfo实例列表(应该只有一个元素)
     */
    public List<AclInfo> checkAclInfo(String api, String method, String clientName) {
        try {
            return this.templateSys.query(
                    "SELECT `i`.`api` AS `api`, `i`.`method` AS `method`, `c`.`name` AS `client_name`, `a`.`duration` AS `duration` " +
                    "FROM `acl` AS `a` INNER JOIN `interfaces` AS `i` ON `a`.`interface_id` = `i`.`id` INNER JOIN `clients` AS `c` ON `a`.`client_id` = `c`.`id` " +
                    "WHERE `i`.`api` = ? AND `i`.`method` = ? AND `c`.`name` = ?",
                    new Object[] {api, method, clientName},
                    (ResultSet rs, int rowNum) -> new AclInfo(
                            rs.getString("api"),
                            rs.getString("method"),
                            rs.getString("client_name"),
                            rs.getString("duration")
                    )
            );
        }
        catch (Exception ex) {
            // 不向上层抛出异常,上层用返回值区别结果
            System.err.println("==> got no acl from DB: " + ex.getMessage());
            return null;
        }
    }


    /**
     * 授权某interface给某用户(新建acl记录)
     * 会清除db中原有符合interface id和client name的记录
     * @param interfaceId 接口id
     * @param clientName client名
     * @param duration 时间段
     */
    @Transactional("txManager")
    public void grantInterfaceToClient(int interfaceId, String clientName, String duration) {
        this.templateSys.update(
                "DELETE FROM `acl` WHERE `interface_id` = ? AND `client_id` = (SELECT `id` FROM `clients` WHERE `name` = ?)",
                interfaceId, clientName
        );

        this.templateSys.update(
                "INSERT INTO `acl` (interface_id, client_id, duration) VALUES (?, (SELECT `id` FROM `clients` WHERE `name` = ?), ?)",
                interfaceId, clientName, duration
        );
    }

    /**
     * 剥夺某用户对于某接口的权限(删除acl记录)
     * 从db中清除后回尝试清理缓存
     * @param interfaceId 接口id
     * @param clientName client名
     */
    public void revokeInterfaceFromClient(int interfaceId, String clientName) {
        this.templateSys.update(
                "DELETE FROM `acl` WHERE `interface_id` = ? AND `client_id` = (SELECT `id` FROM `clients` WHERE `name` = ?)",
                interfaceId, clientName
        );
    }

    /**
     * 获取所有表信息
     * @return TableInfo的列表
     */
    public List<TableInfo> getTableInfo() {
        return this.templateSys.query(
                "SELECT `id`, `name`, `db_name`, `owner_id` FROM `tables`",
                (ResultSet rs, int rowNum) -> new TableInfo(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("db_name"),
                        rs.getInt("owner_id")
                )
        );
    }

    /**
     * 根据表名和数据库名获取表信息
     * @param dbName 数据库名
     * @param tableName 表名
     * @return 表信息列表
     */
    public TableInfo getTableInfoBy(String dbName, String tableName) {
        return this.templateSys.queryForObject(
                "SELECT `id`, `name`, `db_name`, `owner_id` FROM `tables` WHERE `name` = ? AND `db_name` = ?",
                new Object[] {tableName, dbName},
                (ResultSet rs, int rowNum) -> new TableInfo(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("db_name"),
                        rs.getInt("owner_id")
                )
        );
    }


    /**
     * 获取所有API信息
     * @return InterfaceInfo的列表
     */
    public List<InterfaceInfo> getInterfaceInfo() {
        return this.templateSys.query(
                "SELECT `id`, `table_name`, `db_name`, `api`, `method`, `description` FROM `interfaces`",
                (ResultSet rs, int rowNum) -> new InterfaceInfo(
                        rs.getInt("id"),
                        rs.getString("table_name"),
                        rs.getString("db_name"),
                        rs.getString("api"),
                        rs.getString("method"),
                        rs.getString("description")
                )
        );
    }

    /**
     * 根据数据库名和表名获取api信息
     * @param dbName 数据库名
     * @param tableName 表名
     * @return InterfaceInfo列表
     */
    public List<InterfaceInfo> getInterfaceInfoBy(String dbName, String tableName) {
        return this.templateSys.query(
                "SELECT `id`, `table_name`, `db_name`, `api`, `method`, `description` FROM `interfaces` " +
                "WHERE `table_name` = ? AND `db_name` = ?",
                new Object[] {tableName, dbName},
                (ResultSet rs, int rowNum) -> new InterfaceInfo(
                        rs.getInt("id"),
                        rs.getString("table_name"),
                        rs.getString("db_name"),
                        rs.getString("api"),
                        rs.getString("method"),
                        rs.getString("description")
                )
        );
    }

    /**
     * 根据接口id获取接口信息
     * @param id 接口id
     * @return InterfaceInfo实例
     */
    public InterfaceInfo getInterfaceInfoById(int id) {
        return this.templateSys.queryForObject(
                "SELECT `id`,`table_name`,`db_name`,`api`,`method`,`description` " +
                "FROM `interfaces` WHERE `id` = ?",
                new Object[] {id},
                (ResultSet rs, int rowNum) -> new InterfaceInfo(
                        rs.getInt("id"),
                        rs.getString("table_name"),
                        rs.getString("db_name"),
                        rs.getString("api"),
                        rs.getString("method"),
                        rs.getString("description")
                )
        );
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
                "INSERT INTO `tables` (`name`,`db_name`,`owner_id`,`created_at`) " +
                "VALUES (?, ?, ?, now())";
        String sql_add_interface =
                "INSERT INTO `interfaces` (`table_name`,`db_name`,`api`,`method`,`created_at`) " +
                "VALUES (?, ?, ?, ?, now())";

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


}
