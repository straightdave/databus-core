package com.blueline.databus.core.dao;

import com.blueline.databus.core.datatype.AclInfo;
import com.blueline.databus.core.datatype.ClientInfo;
import com.blueline.databus.core.datatype.InterfaceInfo;
import com.blueline.databus.core.datatype.TableInfo;
import com.blueline.databus.core.exception.InternalException;
import com.blueline.databus.core.helper.RandomStringHelper;
import com.blueline.databus.core.helper.SQLParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
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
                "SELECT `id`,`name`,`display_name`,`description`,`status`," +
                        "`appkey`,`skey`,`client_type`,`client_category`," +
                        "`vendor_name`, `vendor_ext` " +
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
                        rs.getString("client_category"),
                        rs.getString("vendor_name"),
                        rs.getString("vendor_ext")
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
                "SELECT `id`,`name`,`display_name`,`description`,`status`," +
                        "`appkey`,`skey`,`client_type`,`client_category`," +
                        "`vendor_name`, `vendor_ext` " +
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
                        rs.getString("client_category"),
                        rs.getString("vendor_name"),
                        rs.getString("vendor_ext")
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
                "SELECT `id`,`name`,`display_name`,`description`,`status`," +
                        "`appkey`,`skey`,`client_type`,`client_category`," +
                        "`vendor_name`,`vendor_ext` " +
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
                        rs.getString("client_category"),
                        rs.getString("vendor_name"),
                        rs.getString("vendor_ext")
                )
        );
    }

    /**
     * 获取所有client信息
     * @return ClientInfo实例列表
     */
    public List<ClientInfo> getClients() {
        return this.templateSys.query(
                "SELECT `id`, `name`, `display_name`, `description`,`status`, " +
                        "`appkey`, `skey`, `client_type`, `client_category`," +
                        "`vendor_name`, `vendor_ext` " +
                "FROM `clients`",
                (ResultSet rs, int i) -> new ClientInfo(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("display_name"),
                        rs.getString("description"),
                        rs.getInt("status"),
                        rs.getString("appkey"),
                        rs.getString("skey"),
                        rs.getString("client_type"),
                        rs.getString("client_category"),
                        rs.getString("vendor_name"),
                        rs.getString("vendor_ext")
                )
        );
    }


    /**
     * 创建新client，参数是json格式的简单字典，需要以下字段：
     * <ul>
     * <li>name 账户名称(唯一、不变)</li>
     * <li>displayName 显示名称</li>
     * <li>description 描述</li>
     * <li>clientType 类型</li>
     * <li>clientCategory 范畴</li>
     * <li>vendor_name 第三方平台提供的账户名称</li>
     * <li>vendor_ext 第三方平台提供的额外信息</li>
     * </ul>
     *
     * <p>
     *     注意：以上字段中，name是必须字段；如果不提供name字段，则必须提供vendor_name字段值；
     *     如果同时提供了name和vendor_name，则name取vendor_name值
     * </p>
     * @return ClientInfo实例
     */
    public ClientInfo createClient(String jsonBody) throws InternalException {
        ClientInfo c = parseCreateClientInfo(jsonBody);
        String newAppKey = RandomStringHelper.getRandomString(10);
        String newSKey = RandomStringHelper.hashKey(newAppKey);

        String display_name = StringUtils.isEmpty(c.getDisplayName())    ? c.getName() : c.getDisplayName();
        String description  = StringUtils.isEmpty(c.getDescription())    ? "" : c.getDescription();
        String type         = StringUtils.isEmpty(c.getClientType())     ? "web" : c.getClientType();
        String category     = StringUtils.isEmpty(c.getClientCategory()) ? "internal" : c.getClientCategory();
        String vendor_name  = StringUtils.isEmpty(c.getVendorName())     ? "" : c.getVendorName();
        String vendor_ext   = StringUtils.isEmpty(c.getVendorExt())      ? "" : c.getVendorExt();

        this.templateSys.update(
                "INSERT INTO `clients`(" +
                        "`name`,`display_name`,`description`,`appkey`,`skey`,`client_type`," +
                        "`client_category`,`vendor_name`,`vendor_ext`) " +
                "VALUES (?,?,?,?,?,?,?,?,?)",
                c.getName(), display_name, description, newAppKey, newSKey, type,
                category, vendor_name, vendor_ext
        );
        return getClientByName(c.getName());
    }

    /**
     * 从json格式字典中读取所需字段，返回ClientInfo实例；
     * 如果vendor_name不为空，它的值会覆盖name字段；如果name和vendor_name都为空，则报错
     * @param jsonBody json格式字典
     * @return 记载信息的clientInfo实例
     * @throws InternalException 内部异常
     */
    private ClientInfo parseCreateClientInfo(String jsonBody) throws InternalException {
        ObjectMapper om = new ObjectMapper();
        Map<String, String> rawBody;
        try {
            rawBody = om.readValue(jsonBody, Map.class);
        }
        catch (IOException | ClassCastException ex) {
            throw new InternalException("parse json from request body failed: " + ex.getMessage());
        }

        String name = rawBody.get("name");
        String vendor_name = rawBody.get("vendor_name");
        if (!StringUtils.isEmpty(vendor_name)) {
            name = vendor_name;
        }
        else if (StringUtils.isEmpty(name) &&
                 StringUtils.isEmpty(vendor_name)) {
            throw new InternalException("both name and vendor_name are blank");
        }

        return new ClientInfo(0,
                name,
                rawBody.get("display_name"),
                rawBody.get("description"),
                0, "", "",
                rawBody.get("type"),
                rawBody.get("category"),
                vendor_name,
                rawBody.get("vendor_ext")
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
     * （测试使用only）从系统表中删除一个client信息
     * @param name client名称
     * @return 受影响行数
     */
    public int deleteClient(String name) {
        return this.templateSys.update("DELETE FROM `clients` WHERE `name` = ?", name);
    }

    /**
     * 重置client的appkey和skey
     * @param name client名
     * @return 受影响行数
     */
    public int resetKeys(String name) {
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
            "SELECT `i`.`api` AS `api`, `i`.`method` AS `method`, " +
                    "`c`.`appkey` AS `client_appkey`, `c`.`name` AS `client_name`, " +
                    "`a`.`duration` AS `duration`,`a`.`created_at` AS `created_at` " +
            "FROM `acl` AS `a` INNER JOIN `interfaces` AS `i` ON `a`.`interface_id` = `i`.`id` INNER JOIN `clients` AS `c` ON `a`.`client_id` = `c`.`id`",
            (ResultSet rs, int rowNum) -> new AclInfo(
                    rs.getString("api"),
                    rs.getString("method"),
                    rs.getString("client_appkey"),
                    rs.getString("client_name"),
                    rs.getString("duration"),
                    rs.getTimestamp("created_at")
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
            "SELECT `i`.`api` AS `api`, `i`.`method` AS `method`, " +
                    "`c`.`appkey` AS `client_appkey`, `c`.`name` AS `client_name`, " +
                    "`a`.`duration` AS `duration`,`a`.`created_at` AS `created_at` " +
            "FROM `acl` AS `a` INNER JOIN `interfaces` AS `i` ON `a`.`interface_id` = `i`.`id` INNER JOIN `clients` AS `c` ON `a`.`client_id` = `c`.`id` " +
            "WHERE `i`.`id` = ?",
            new Object[] {interfaceId},
            (ResultSet rs, int rowNum) -> new AclInfo(
                    rs.getString("api"),
                    rs.getString("method"),
                    rs.getString("client_appkey"),
                    rs.getString("client_name"),
                    rs.getString("duration"),
                    rs.getTimestamp("created_at")
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
                "SELECT `i`.`api` AS `api`, `i`.`method` AS `method`, " +
                        "`c`.`appkey` AS `client_appkey`, `c`.`name` AS `client_name`, " +
                        "`a`.`duration` AS `duration`,`a`.`created_at` AS `created_at` " +
                "FROM `acl` AS `a` INNER JOIN `interfaces` AS `i` ON `a`.`interface_id` = `i`.`id` INNER JOIN `clients` AS `c` ON `a`.`client_id` = `c`.`id` " +
                "WHERE `c`.`name` = ?",
                new Object[] {clientName},
                (ResultSet rs, int rowNum) -> new AclInfo(
                        rs.getString("api"),
                        rs.getString("method"),
                        rs.getString("client_appkey"),
                        rs.getString("client_name"),
                        rs.getString("duration"),
                        rs.getTimestamp("created_at")
                )
        );
    }

    /**
     * 根据条件获取acl信息
     * @param api api路径
     * @param method http方法
     * @param appKey 访问者appKey
     * @return AclInfo实例列表(其理论上应该只有一个元素)
     */
    public List<AclInfo> checkAclInfoByAppKey(String api, String method, String appKey) {
        try {
            return this.templateSys.query(
                    "SELECT `i`.`api` AS `api`, `i`.`method` AS `method`, " +
                            "`c`.`appkey` AS `client_appkey`, `c`.`name` AS `client_name`, " +
                            "`a`.`duration` AS `duration`,`a`.`created_at` AS `created_at` " +
                    "FROM `acl` AS `a` INNER JOIN `interfaces` AS `i` ON `a`.`interface_id` = `i`.`id` INNER JOIN `clients` AS `c` ON `a`.`client_id` = `c`.`id` " +
                    "WHERE `i`.`api` = ? AND `i`.`method` = ? AND `c`.`appkey` = ?",
                    new Object[] {api, method, appKey},
                    (ResultSet rs, int rowNum) -> new AclInfo(
                            rs.getString("api"),
                            rs.getString("method"),
                            rs.getString("client_appkey"),
                            rs.getString("client_name"),
                            rs.getString("duration"),
                            rs.getTimestamp("created_at")
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
     * 判断是否是interface的owner（即，interface所属的表的owner）
     * @param appkey 客户端app key
     * @param interfaceId 接口id
     * @return true/false
     */
    public boolean isInterfaceOwner(String appkey, int interfaceId) {
        List<InterfaceInfo> interfaceInfoList = this.templateSys.query(
                "SELECT COUNT(1) FROM `interfaces` " +
                "WHERE `interface_id` = ? AND (`table_name`, `db_name`) = " +
                        "(SELECT `name`, `db_name` FROM `tables` WHERE `owner_id` = " +
                            "(SELECT `id` FROM `clients` WHERE `appkey` = ?))",
                new Object[] {interfaceId, appkey},
                (ResultSet rs, int rowNum) -> new InterfaceInfo(
                        rs.getInt("id"),
                        rs.getString("table_name"),
                        rs.getString("db_name"),
                        rs.getString("api"),
                        rs.getString("method"),
                        rs.getString("description")
                )
        );
        return !interfaceInfoList.isEmpty();
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
                String.format(
                        "DELETE FROM `acl` WHERE `interface_id` = '%s' AND `client_id` = (SELECT `id` FROM `clients` WHERE `name` = '%s')",
                        interfaceId, clientName
                )
        );

        this.templateSys.update(
                String.format(
                        "INSERT INTO `acl` (`interface_id`, `client_id`, `duration`,`created_at`) VALUES ('%s', (SELECT `id` FROM `clients` WHERE `name` = '%s'), '%s', now())",
                        interfaceId, clientName, duration
                )
        );
    }

    /**
     * 将表的所有权限授予某客户
     * @param dbName 库名
     * @param tableName 表名
     * @param clientName 客户端名称
     */
    @Transactional("txManager")
    public void grantInterfaceToClientByTable(String dbName, String tableName, String clientName) {
        int client_id = getClientByName(clientName).getId();

        this.templateSys.update(
                "DELETE FROM `acl` " +
                "WHERE `interface_id` IN (SELECT `id` FROM `interfaces` WHERE `table_name` = ? AND `db_name` = ?) AND `client_id` = ?",
                tableName, dbName, client_id
        );

        this.templateSys.update(
                "INSERT INTO `acl` (`interface_id`,`client_id`,`created_at`) " +
                "SELECT `id`, ?, now() " +
                "FROM `interfaces` " +
                "WHERE `table_name` = ? AND `db_name` = ?",
                client_id, tableName, dbName
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
                "SELECT `t`.`id`, `t`.`name`, `t`.`db_name`, `t`.`owner_id`, " +
                        "`c`.`name` AS `owner_name`, `t`.`description`, `t`.`created_at`, " +
                        "`m`.`UPDATE_TIME`, (`m`.`DATA_LENGTH` + `m`.`INDEX_LENGTH`) AS `TABLE_SIZE`, " +
                        "`m`.`TABLE_ROWS` " +
                        "FROM `tables` AS `t` INNER JOIN `information_schema`.`tables` AS `m` " +
                        "JOIN `clients` AS `c` " +
                        "ON `t`.`name` = `m`.`table_name` AND `t`.`db_name` = `m`.`table_schema` " +
                        "AND `t`.`owner_id` = `c`.`id`",
                (ResultSet rs, int rowNum) -> new TableInfo(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("db_name"),
                        rs.getInt("owner_id"),
                        rs.getString("owner_name"),
                        rs.getString("description"),
                        rs.getTimestamp("created_at"),
                        rs.getTimestamp("UPDATE_TIME"),
                        rs.getInt("TABLE_SIZE"),
                        rs.getInt("TABLE_ROWS")
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
                "SELECT `t`.`id`, `t`.`name`, `t`.`db_name`, `t`.`owner_id`, " +
                "`c`.`name` AS `owner_name`, `t`.`description`, `t`.`created_at`, " +
                "`m`.`UPDATE_TIME`, (`m`.`DATA_LENGTH` + `m`.`INDEX_LENGTH`) AS `TABLE_SIZE`, " +
                "`m`.`TABLE_ROWS` " +
                "FROM `tables` AS `t` INNER JOIN `information_schema`.`tables` AS `m` " +
                        "JOIN `clients` AS `c` " +
                        "ON `t`.`name` = `m`.`table_name` AND `t`.`db_name` = `m`.`table_schema` " +
                        "AND `t`.`owner_id` = `c`.`id` " +
                "WHERE `t`.`name` = ? AND `t`.`db_name` = ?",
                new Object[] {tableName, dbName},
                (ResultSet rs, int rowNum) -> new TableInfo(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("db_name"),
                        rs.getInt("owner_id"),
                        rs.getString("owner_name"),
                        rs.getString("description"),
                        rs.getTimestamp("created_at"),
                        rs.getTimestamp("UPDATE_TIME"),
                        rs.getInt("TABLE_SIZE"),
                        rs.getInt("TABLE_ROWS")
                )
        );
    }

    /**
     * 根据客户端名称返回其所有表的信息
     * @param clientName 客户端名称
     * @return 表信息列表
     */
    public List<TableInfo> getTablesByClient(String clientName) {
        return this.templateSys.query(
                "SELECT `t`.`id`, `t`.`name`, `t`.`db_name`, `t`.`owner_id`, " +
                "`t`.`description`, `t`.`created_at`, " +
                "`m`.`UPDATE_TIME`, (`m`.`DATA_LENGTH` + `m`.`INDEX_LENGTH`) AS `TABLE_SIZE`, " +
                "`m`.`TABLE_ROWS` " +
                "FROM `tables` AS `t` INNER JOIN `information_schema`.`tables` AS `m` " +
                "ON `t`.`name` = `m`.`table_name` AND `t`.`db_name` = `m`.`table_schema` " +
                "WHERE `t`.`owner_id` = (SELECT `id` FROM `clients` WHERE `name` = ?)",
                new Object[] {clientName},
                (ResultSet rs, int rowNum) -> new TableInfo(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("db_name"),
                        rs.getInt("owner_id"),
                        clientName,
                        rs.getString("description"),
                        rs.getTimestamp("created_at"),
                        rs.getTimestamp("UPDATE_TIME"),
                        rs.getInt("TABLE_SIZE"),
                        rs.getInt("TABLE_ROWS")
                )
        );
    }

    /**
     * 获取所有API信息
     * @return InterfaceInfo的列表
     */
    public List<InterfaceInfo> getAllInterfaceInfo() {
        return this.templateSys.query(
                "SELECT `id`, `table_name`, `db_name`, `api`, `method`, `description` " +
                "FROM `interfaces`",
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
    public List<InterfaceInfo> getInterfaceInfoByTable(String dbName, String tableName) {
        return this.templateSys.query(
                "SELECT `id`, `table_name`, `db_name`, `api`, `method`, `description` " +
                "FROM `interfaces` " +
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
    public void doAfterTableCreated(String dbName, String tableName, int ownerId, String description)
            throws InternalException {

        String sql_add_table =
                "INSERT INTO `tables` (`name`,`db_name`,`owner_id`,`description`, `created_at`) " +
                "VALUES (?, ?, ?, ?, now())";
        String sql_add_interface =
                "INSERT INTO `interfaces` (`table_name`,`db_name`,`api`,`method`,`description`,`created_at`) " +
                "VALUES (?, ?, ?, ?, ?, now())";

        String sql_grant =
                "INSERT INTO `acl` (`interface_id`,`client_id`,`created_at`) " +
                "SELECT `id`, ?, now() " +
                "FROM `interfaces` " +
                "WHERE `table_name` = ? AND `db_name` = ?";

        Connection conn = null;
        try {
            conn = this.templateSys.getDataSource().getConnection();
            conn.setAutoCommit(false);

            // add new entry to table `tables`
            PreparedStatement ps = conn.prepareStatement(sql_add_table);
            ps.setString(1, tableName);
            ps.setString(2, dbName);
            ps.setInt(3, ownerId);
            ps.setString(4, description);
            ps.executeUpdate();

            // add 'insert' api to table `interfaces`
            PreparedStatement ps1 = conn.prepareStatement(sql_add_interface);
            ps1.setString(1, tableName);
            ps1.setString(2, dbName);
            ps1.setString(3, "/api/data/" + dbName + "/" + tableName);
            ps1.setString(4, "POST");
            ps1.setString(5, String.format("向表{%s.%s}插入数据",dbName,tableName));
            ps1.executeUpdate();

            // add 'delete' api to table `interfaces`
            PreparedStatement ps2 = conn.prepareStatement(sql_add_interface);
            ps2.setString(1, tableName);
            ps2.setString(2, dbName);
            ps2.setString(3, "/api/data/" + dbName + "/" + tableName);
            ps2.setString(4, "DELETE");
            ps2.setString(5, String.format("删除表{%s.%s}数据",dbName,tableName));
            ps2.executeUpdate();

            // add 'update' api to table `interfaces`
            PreparedStatement ps3 = conn.prepareStatement(sql_add_interface);
            ps3.setString(1, tableName);
            ps3.setString(2, dbName);
            ps3.setString(3, "/api/data/" + dbName + "/" + tableName);
            ps3.setString(4, "PUT");
            ps3.setString(5, String.format("修改表{%s.%s}数据",dbName,tableName));
            ps3.executeUpdate();

            // add 'select' api to table `interfaces`
            PreparedStatement ps4 = conn.prepareStatement(sql_add_interface);
            ps4.setString(1, tableName);
            ps4.setString(2, dbName);
            ps4.setString(3, "/api/data/" + dbName + "/" + tableName);
            ps4.setString(4, "GET");
            ps4.setString(5, String.format("从表{%s.%s}查询数据",dbName,tableName));
            ps4.executeUpdate();

            // grant table's interfaces to client (owner)
            PreparedStatement ps5 = conn.prepareStatement(sql_grant);
            ps5.setInt(1, ownerId);
            ps5.setString(2, tableName);
            ps5.setString(3, dbName);
            ps5.executeUpdate();

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
                throw new InternalException("post-table-creation exception (roll-back failed):" + ex.getMessage());
            }
            throw new InternalException("post-creation action exception (roll-back done): " + ex.getMessage());
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
     * 重载，参数是owner_name取代owner_id
     * @param dbName 库名
     * @param tableName 表名
     * @param ownerName 客户端名称
     * @param description 表描述
     * @throws InternalException 内部异常
     */
    public void doAfterTableCreated(String dbName, String tableName, String ownerName, String description)
            throws InternalException {
        int owner_id = -1;
        try {
            owner_id = getClientByName(ownerName).getId();
            doAfterTableCreated(dbName, tableName, owner_id, description);
        }
        catch (Exception ex) {
            throw new InternalException(
                    String.format(
                            "Calling doAfterTableCreated with owner_name = {%s} (owner_id={%s}); err: {%s}",
                            ownerName, owner_id, ex.getMessage()));
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

        String sql_revoke =
                "DELETE FROM `acl` " +
                "WHERE `interface_id` IN (SELECT `id` FROM `interfaces` WHERE `table_name` = ? AND `db_name` = ?)";

        String sql_delete_table =
                "DELETE FROM `tables` WHERE `db_name` = ? AND `name` = ?";
        String sql_delete_interface =
                "DELETE FROM `interfaces` WHERE `db_name` = ? AND `table_name` = ?";

        Connection conn = null;
        try {
            conn = this.templateSys.getDataSource().getConnection();
            conn.setAutoCommit(false);

            PreparedStatement ps0 = conn.prepareStatement(sql_revoke);
            ps0.setString(1, tableName);
            ps0.setString(2, dbName);
            ps0.executeUpdate();

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
