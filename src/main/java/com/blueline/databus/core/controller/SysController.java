package com.blueline.databus.core.controller;

import com.blueline.databus.core.dao.AclCacheService;
import com.blueline.databus.core.dao.ApiRecordService;
import com.blueline.databus.core.dao.SysDBDao;
import com.blueline.databus.core.datatype.*;
import com.blueline.databus.core.helper.SQLParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * admin的操作接口;
 * 提供了管理平台对系统表(保存了数据表的meta信息等数据)对象的访问;
 * 只有admin客户端(根据请求头中的appkey判断)才能访问
 */
@RestController
@RequestMapping("/api/sys")
public class SysController {
    private final Logger logger = Logger.getLogger(SysController.class);
    private ObjectMapper om = new ObjectMapper();

    @Value("${admin.appkey}")
    private String adminAppKey;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private SysDBDao sysDBDao;

    @Autowired
    private AclCacheService aclCacheService;

    @Autowired
    private ApiRecordService apiRecordService;

    /**
     * 获取所有客户端信息;
     * <p>
     *     <strong>Admin Only!</strong>
     * </p>
     *
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
            if (!request.getHeader("x-appkey").equalsIgnoreCase(adminAppKey)) {
                return new RestResult(ResultType.FAIL, "admin only");
            }

            List<ClientInfo> clients = sysDBDao.getClients();
            return new RestResult(ResultType.OK, om.writeValueAsString(clients));
        }
        catch (Exception ex) {
            logger.fatal(ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }

    /**
     * 创建客户端
     * @param jsonBody json格式字典，必须包含若干字段(name),也可包含字段(display_name,description,type,category)
     * @return RestResult实例，数据部分是新创建的客户端clientInfo(json格式)
     * @see SysDBDao#createClient(String)
     */
    @RequestMapping(value = "/clients", method = POST)
    public RestResult createClient(@RequestBody String jsonBody) {
        try {
            if (!request.getHeader("x-appkey").equalsIgnoreCase(adminAppKey)) {
                return new RestResult(ResultType.FAIL, "admin only");
            }

            ClientInfo newClient = sysDBDao.createClient(jsonBody);
            return new RestResult(ResultType.OK, om.writeValueAsString(newClient));
        }
        catch (Exception ex) {
            logger.fatal(ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }

    /**
     * 根据客户端名称获取客户端信息
     *
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
     * 根据客户端id获取客户端信息;
     * <p>
     *     <strong>Admin Only!</strong>
     * </p>
     *
     * <pre>
     *     <code>GET /api/sys/client/id_{id}</code>
     * </pre>
     *
     * @param id 客户端Id(不变、唯一)
     * @return ClientInfo实例
     * @see ClientInfo
     */
    @RequestMapping(value = "/client/id_{id}", method = GET)
    public RestResult getClientById(@PathVariable(value = "id") String id) {
        try {
            if (!request.getHeader("x-appkey").equalsIgnoreCase(adminAppKey)) {
                return new RestResult(ResultType.FAIL, "admin only");
            }

            ClientInfo client = sysDBDao.getClientByID(Integer.valueOf(id));
            if (client != null) {
                return new RestResult(ResultType.OK, om.writeValueAsString(client));
            }
            else {
                return new RestResult(ResultType.FAIL, String.format("got no client with Id {%s}", id));
            }
        }
        catch (Exception ex) {
            logger.fatal(ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }

    /**
     * 返回客户端所拥有的所有的表的信息
     * @param clientName 客户端名称
     * @return RestResult的message字段中是json格式的表信息的列表
     */
    @RequestMapping(value = "/client/{name}/tables", method = GET)
    public RestResult getTablesByClient(@PathVariable(value = "name") String clientName) {
        try {
            if (!request.getHeader("x-appkey").equalsIgnoreCase(adminAppKey)) {
                return new RestResult(ResultType.FAIL, "admin only");
            }

            List<TableInfo> result = sysDBDao.getTablesByClient(clientName);
            return new RestResult(ResultType.OK, om.writeValueAsString(result));
        }
        catch (Exception ex) {
            logger.fatal(ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }

    /**
     * 获取某个客户端拥有的Acl记录(对接口的访问权限);
     * <p>
     *     <strong>Admin Only!</strong>
     * </p>
     *
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
            if (!request.getHeader("x-appkey").equalsIgnoreCase(adminAppKey)) {
                return new RestResult(ResultType.FAIL, "admin only");
            }

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
     *
     * <p>
     *     <strong>Admin and Owner Only!</strong>
     * </p>
     *
     * <pre>
     *     <code>POST /api/sys/client/{name}/reset</code>
     * </pre>
     *
     * @param name 客户端名称
     * @return 操作结果以及最新的clientInfo实例
     * @see SysDBDao#resetKeys(String)
     * @see ClientInfo
     */
    @RequestMapping(value = "/client/{name}/reset", method = POST)
    public RestResult resetClientKeys(
            @PathVariable("name") String name
    ) {
        try {
            if (!request.getHeader("x-appkey").equalsIgnoreCase(adminAppKey)) {
                return new RestResult(ResultType.FAIL, "admin only");
            }

            int count = sysDBDao.resetKeys(name);
            if (count == 1) {
                ClientInfo c = sysDBDao.getClientByName(name);
                return new RestResult(ResultType.OK, c.toString());
            }
            else {
                return new RestResult(ResultType.FAIL, String.format("%s rows (should be 1) affected", count));
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
            if (!request.getHeader("x-appkey").equalsIgnoreCase(adminAppKey)) {
                return new RestResult(ResultType.FAIL, "admin only");
            }

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
            ClientInfo client = sysDBDao.getClientByID(table.getOwnerId());
            client.hideKeys();

            Map<String, Object> result = new HashMap<>();
            result.put("table", table);
            result.put("owner", client);

            return new RestResult(ResultType.OK, om.writeValueAsString(result));
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
        if (!request.getHeader("x-appkey").equalsIgnoreCase(adminAppKey)) {
            return new RestResult(ResultType.FAIL, "admin only");
        }

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
        if (!request.getHeader("x-appkey").equalsIgnoreCase(adminAppKey)) {
            return new RestResult(ResultType.FAIL, "admin only");
        }

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
     * 在sys数据库中添加acl记录后,还会立刻向缓存中添加记录;
     *
     * <p>
     *     <strong>Admin and Owner Only!</strong>
     * </p>
     *
     * <pre>
     *     <code>POST /api/sys/interface/{id}/grant_to/{clientName}</code>
     * </pre>
     *
     * <p>
     *     请求体数据:duration=11001900，默认是"0"
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
    @RequestMapping(value = "/interface/{id}/grant_to/{clientName}", method = POST)
    public RestResult grantInterfaceToClient(
            @PathVariable("id") int id,
            @PathVariable("clientName") String clientName,
            @RequestParam(name = "duration", required = false, defaultValue = "0") String duration
    ) {
        System.out.println("client name => " + clientName);

        try {
            String appkey = request.getHeader("x-appkey");
            if (!appkey.equalsIgnoreCase(adminAppKey))
                if (!sysDBDao.isInterfaceOwner(appkey, id))
                    return new RestResult(ResultType.FAIL, "admin or interface owner only");
            sysDBDao.grantInterfaceToClient(id, clientName, duration);
            return new RestResult(ResultType.OK, String.format(
                    "granted interface{%s} to client{%s} with duration{%s}",id, clientName, duration));
        }
        catch (Exception ex) {
            logger.fatal(ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }

    /**
     * 剥夺某客户端对某接口的访问权限(删除acl记录);
     * <p>
     *     <strong>Admin and Owner Only!</strong>
     * </p>
     *
     * <pre>
     *     <code>DELETE /api/sys/interface/{id}/revoke_from/{clientName}</code>
     * </pre>
     *
     * @param id 接口id
     * @param clientName 客户端名称
     * @return 结果信息字符串
     * @see #grantInterfaceToClient(int, String, String)
     */
    @RequestMapping(value = "/interface/{id}/revoke_from/{clientName}", method = DELETE)
    public RestResult revokeInterfaceFromClient(
            @PathVariable("id")         int id,
            @PathVariable("clientName") String clientName
    ) {
        try {
            String appkey = request.getHeader("x-appkey");
            if (!appkey.equalsIgnoreCase(adminAppKey))
                if (!sysDBDao.isInterfaceOwner(appkey, id))
                    return new RestResult(ResultType.FAIL, "admin or interface owner only");

            ClientInfo clientInfo = sysDBDao.getClientByName(clientName);
            sysDBDao.revokeInterfaceFromClient(id, clientName);

            InterfaceInfo interfaceInfo = sysDBDao.getInterfaceInfoById(id);
            String cacheKey = String.format("%s %s", interfaceInfo.getMethod().toUpperCase(), interfaceInfo.getApi());
            aclCacheService.removeOneAcl(cacheKey, clientInfo.getAppKey());
            return new RestResult(ResultType.OK, String.format("revoked interface{%s} from client{%s}", id, clientName));
        }
        catch (Exception ex) {
            logger.fatal(ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }

    /**
     * Dump目前缓存中保存的所有acl记录;
     * <p>
     *     <strong>admin only!</strong>
     * </p>
     *
     * <pre>
     *     <code>GET /api/sys/acl_cache</code>
     * </pre>
     *
     * @return AclInfo实例列表
     * @see AclInfo
     */
    @RequestMapping(value = "/acl_cache", method = GET)
    public RestResult dumpAclCache() {
        try {
            if (!request.getHeader("x-appkey").equalsIgnoreCase(adminAppKey)) {
                return new RestResult(ResultType.FAIL, "admin only");
            }

            List<AclInfo> aclInfoList = aclCacheService.dumpAllAcl();
            return new RestResult(ResultType.OK, om.writeValueAsString(aclInfoList));
        }
        catch (Exception ex) {
            logger.fatal(ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }

    /**
     * Dump记录在缓存中得所有api调用计数;
     * <p>
     *     <strong>admin only!</strong>
     * </p>
     *
     * <pre>
     *     <code>GET /api/sys/call_records</code>
     * </pre>
     *
     * @return api调用计数信息
     */
    @RequestMapping(value = "/call_records", method = GET)
    public RestResult dumpCallRecord() {
        try {
            if (!request.getHeader("x-appkey").equalsIgnoreCase(adminAppKey)) {
                return new RestResult(ResultType.FAIL, "admin only");
            }

            Map<String, Integer> record = apiRecordService.dumpAllCallRecord();
            return new RestResult(ResultType.OK, om.writeValueAsString(record));
        }
        catch (Exception ex) {
            logger.fatal(ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }
}
