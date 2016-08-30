package com.blueline.databus.core.controller;

import com.blueline.databus.core.dao.AclCacheService;
import com.blueline.databus.core.dao.SysDBDao;
import com.blueline.databus.core.datatype.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * admin的操作接口;
 * 提供了管理平台对系统表(保存了数据表的meta信息等数据)对象的访问;
 * 只有admin客户端(根据请求头中的appkey判断)才能访问
 */
@RestController
@RequestMapping("/api/sys")
public class AdminController {
    private final Logger logger = Logger.getLogger(AdminController.class);
    private ObjectMapper om = new ObjectMapper();

    @Autowired
    private SysDBDao sysDBDao;

    @Autowired
    private AclCacheService aclCacheService;

    /**
     * 获取所有客户端信息
     * <pre>
     *     <code>GET /api/sys/clients</code>
     * </pre>
     *
     * @return RestResult实例(json格式),message字段包含ClientInfo实例列表(json格式)/错误信息;
     *         本文档其他Controller的action返回数据格式雷同,仅标示出成功时其message字段包含的数据
     * @see RestResult
     * @see ClientInfo
     */
    @RequestMapping(value = "/clients", method = GET)
    public RestResult getAllClients() {
        try {
            List<ClientInfo> clients = sysDBDao.getClients();
            return new RestResult(ResultType.OK, om.writeValueAsString(clients));
        }
        catch (Exception ex) {
            logger.fatal(ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }

    /**
     * 根据客户端名称获取客户端信息
     * <pre>
     *     <code>GET /api/sys/client/{name}</code>
     * </pre>
     *
     * @param name 客户端名称(不变、唯一)
     * @return ClientInfo实例
     * @see ClientInfo
     */
    @RequestMapping(value = "/client/{name}", method = GET)
    public RestResult getClientByName(@PathVariable(value = "name") String name) {
        try {
            ClientInfo client = sysDBDao.getClientByName(name);
            if (client != null) {
                return new RestResult(ResultType.OK, om.writeValueAsString(client));
            }
            else {
                return new RestResult(ResultType.FAIL, String.format("got no client with name {%s}", name));
            }
        }
        catch (Exception ex) {
            logger.fatal(ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }

    /**
     * 获取某个客户端拥有的Acl记录(对接口的访问权限)
     * <pre>
     *     <code>GET /api/sys/client/{name}/acl</code>
     * </pre>
     *
     * @param name 客户端名称
     * @return AclInfo实例列表
     * @see AclInfo
     */
    @RequestMapping(value = "/client/{name}/acl", method = GET)
    public RestResult getClientAclByName(
            @PathVariable(value = "name") String name
    ) {
        try {
            List<AclInfo> aclInfoList = sysDBDao.getAclInfoByClient(name);
            if (aclInfoList != null && aclInfoList.size() > 0) {
                return new RestResult(ResultType.OK, om.writeValueAsString(aclInfoList));
            }
            else {
                return new RestResult(ResultType.FAIL, String.format("got no acl record for client '%s'", name));
            }
        }
        catch (Exception ex) {
            logger.fatal(ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }

    /**
     * 重置客户端的app key和secure key
     * <pre>
     *     <code>POST /api/sys/client/{name}/reset</code>
     * </pre>
     *
     * @param name 客户端名称
     * @return 操作结果信息字符串
     * @see SysDBDao#resetKeys(String)
     * @see ClientInfo
     */
    @RequestMapping(value = "/client/{name}/reset", method = POST)
    public RestResult resetClientKeys(
            @PathVariable("name") String name
    ) {
        try {
            int count = sysDBDao.resetKeys(name);
            if (count == 1) {
                return new RestResult(ResultType.OK, String.format("user {%s} keys reset", name));
            }
            else {
                return new RestResult(ResultType.FAIL, String.format("%s rows affected", count));
            }
        }
        catch (Exception ex) {
            logger.fatal(ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }

    /**
     * 获取所有数据表信息
     * <pre>
     *     <code>GET /api/sys/tables</code>
     * </pre>
     *
     * @return TableInfo实例列表
     * @see TableInfo
     */
    @RequestMapping(value = "/tables", method = GET)
    public RestResult getAllTables() {
        try {
            List<TableInfo> tables = sysDBDao.getTableInfo();
            return new RestResult(ResultType.OK, om.writeValueAsString(tables));
        }
        catch (Exception ex) {
            logger.fatal(ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }

    /**
     * 根据数据库名和表名获取表信息(一般情况下结果唯一)
     * <pre>
     *     <code>GET /api/sys/table/{dbName}/{tableName}</code>
     * </pre>
     *
     * @param dbName 数据库名
     * @param tableName 表名
     * @return TableInfo实例
     * @see TableInfo
     */
    @RequestMapping(value = "/table/{dbName}/{tableName}", method = GET)
    public RestResult getTableBy(
            @PathVariable("dbName")    String dbName,
            @PathVariable("tableName") String tableName
    ) {
        try {
            TableInfo table = sysDBDao.getTableInfoBy(dbName, tableName);
            return new RestResult(ResultType.OK, om.writeValueAsString(table));
        }
        catch (Exception ex) {
            logger.fatal(ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }

    /**
     * 根据数据库名和表名,获取某个数据表的接口信息
     * <pre>
     *     <code>GET /api/sys/interfaces/{dbName}/{tableName}</code>
     * </pre>
     *
     * @param dbName 数据库名
     * @param tableName 表名
     * @return InterfaceInfo实例列表
     * @see InterfaceInfo
     * @see #getInterfaceById(int)
     */
    @RequestMapping(value = "/interfaces/{dbName}/{tableName}", method = GET)
    public RestResult getInterfacesByTable(
            @PathVariable("dbName")    String dbName,
            @PathVariable("tableName") String tableName
    ) {
        try {
            List<InterfaceInfo> apis = sysDBDao.getInterfaceInfoByTable(dbName, tableName);
            return new RestResult(ResultType.OK, om.writeValueAsString(apis));
        }
        catch (Exception ex) {
            logger.fatal(ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }

    /**
     * 根据接口id,获取某接口信息
     * <pre>
     *     <code>GET /api/sys/interface/{id}</code>
     * </pre>
     *
     * @param id 接口id
     * @return InterfaceInfo实例
     * @see InterfaceInfo
     * @see #getInterfacesByTable(String, String)
     */
    @RequestMapping(value = "/interface/{id}", method = GET)
    public RestResult getInterfaceById(
            @PathVariable("id") int id
    ) {
        try {
            InterfaceInfo api = sysDBDao.getInterfaceInfoById(id);
            return new RestResult(ResultType.OK, om.writeValueAsString(api));
        }
        catch (Exception ex) {
            logger.fatal(ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }

    /**
     * 获取涉及某个接口的acl记录
     * <pre>
     *     <code>GET /api/sys/interface/{id}/acl</code>
     * </pre>
     *
     * @param id 接口id
     * @return AclInfo实例列表
     * @see AclInfo
     */
    @RequestMapping(value = "/interface/{id}/acl", method = GET)
    public RestResult getInterfaceAclById(
            @PathVariable("id") int id
    ) {
        try {
            List<AclInfo> aclList = sysDBDao.getAclInfoByInterface(id);
            return new RestResult(ResultType.OK, om.writeValueAsString(aclList));
        }
        catch (Exception ex) {
            logger.fatal("get interface ACL by id error: " + ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }

    /**
     * 为某个客户端赋予某个接口访问权限(增加一条acl记录,关联接口与客户端);
     * 在sys数据库中添加acl记录后,还会立刻向缓存中添加记录
     * <pre>
     *     <code>POST /api/sys/interface/{id}/grant/{clientName}</code>
     * </pre>
     * <p>
     *     请求体数据:duration=11001900
     * </p>
     *
     * @param id 接口id
     * @param clientName 客户端名称
     * @param duration 接口访问时间段;
     *                 八位数字字符表示'HHmmHHmm',即从一天的HHmm到HHmm,如'11001900';
     *                 一位字符'0'表示全天可访问,同'00002359';
     * @return 结果信息字符串
     * @see #revokeInterfaceFromClient(int, String)
     */
    @RequestMapping(value = "/interface/{id}/grant/{clientName}", method = POST)
    public RestResult grantInterfaceToClient(
            @PathVariable("id") int id,
            @PathVariable("clientName") String clientName,
            @RequestParam(name = "duration", required = false) String duration
    ) {
        try {
            if (StringUtils.isEmpty(duration)) {
                duration = "0";
            }
            sysDBDao.grantInterfaceToClient(id, clientName, duration);

            Optional<AclInfo> aclInfo = sysDBDao.getAclInfoByInterface(id)
                                          .stream()
                                          .filter(item -> clientName.equalsIgnoreCase(item.getClientName()))
                                          .findFirst();

            if (aclInfo.isPresent()) {
                aclCacheService.loadOneAcl(aclInfo.get());
            }
            return new RestResult(ResultType.OK, "granted");
        }
        catch (Exception ex) {
            logger.fatal(ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }

    /**
     * 剥夺某客户端对某接口的访问权限(删除acl记录)
     * <pre>
     *     <code>DELETE /api/sys/interface/{id}/revoke/{clientName}</code>
     * </pre>
     *
     * @param id 接口id
     * @param clientName 客户端名称
     * @return 结果信息字符串
     * @see #grantInterfaceToClient(int, String, String)
     */
    @RequestMapping(value = "/interface/{id}/revoke/{clientName}", method = DELETE)
    public RestResult revokeInterfaceFromClient(
            @PathVariable("id")         int id,
            @PathVariable("clientName") String clientName
    ) {
        try {
            Optional<AclInfo> aclInfo = sysDBDao.getAclInfoByInterface(id)
                                                .stream()
                                                .filter(item -> clientName.equalsIgnoreCase(item.getClientName()))
                                                .findFirst();

            if (aclInfo.isPresent()) {
                sysDBDao.revokeInterfaceFromClient(id, clientName);
                aclCacheService.removeOneAcl(aclInfo.get());
                return new RestResult(ResultType.OK, "acl revoked");
            }

            return new RestResult(ResultType.FAIL, "acl not exist");
        }
        catch (Exception ex) {
            logger.fatal(ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }

    /**
     * Dump目前缓存中保存的所有acl记录
     * <pre>
     *     <code>GET /api/sys/acl/dump</code>
     * </pre>
     *
     * @return AclInfo实例列表
     * @see AclInfo
     */
    @RequestMapping(value = "/acl/dump", method = GET)
    public RestResult dumpAclCache() {
        try {
            List<AclInfo> aclInfoList = aclCacheService.dumpAllAcl();
            return new RestResult(ResultType.OK, om.writeValueAsString(aclInfoList));
        }
        catch (Exception ex) {
            logger.fatal(ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }
}
