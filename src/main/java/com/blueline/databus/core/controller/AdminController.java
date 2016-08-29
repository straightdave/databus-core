package com.blueline.databus.core.controller;

import com.blueline.databus.core.dao.AclCacheService;
import com.blueline.databus.core.dao.SysDBDao;
import com.blueline.databus.core.datatype.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.istack.internal.Nullable;
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
 * admin的接口
 * 只有admin(根据其appkey)才能访问
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

    @RequestMapping(value = "/clients", method = GET)
    public RestResult getClients() {
        try {
            List<ClientInfo> clients = sysDBDao.getClients();
            return new RestResult(ResultType.OK, om.writeValueAsString(clients));
        }
        catch (Exception ex) {
            logger.fatal("get client error: " + ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }

    @RequestMapping(value = "/client/{name}", method = GET)
    public RestResult getClientBy(
            @PathVariable(value = "name") String name
    ) {
        try {
            ClientInfo client = sysDBDao.getClientByName(name);
            if (client != null) {
                return new RestResult(ResultType.OK, om.writeValueAsString(client));
            }
            else {
                return new RestResult(ResultType.FAIL, String.format("got no client named as '%s'", name));
            }
        }
        catch (Exception ex) {
            logger.fatal("get client error: " + ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }

    @RequestMapping(value = "/client/{name}/acl", method = GET)
    public RestResult getClientAcl(
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
            logger.fatal("get client error: " + ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }

    @RequestMapping(value = "/client/{name}/reset", method = POST)
    public RestResult resetClientKeys(
            @PathVariable("name") String name
    ) {
        try {
            int count = sysDBDao.refreshKeys(name);
            if (count == 1) {
                return new RestResult(ResultType.OK, String.format("user %s keys reset", name));
            }
            else {
                return new RestResult(ResultType.FAIL, String.format("%s rows affected", count));
            }
        }
        catch (Exception ex) {
            logger.fatal("reset client error: " + ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }

    @RequestMapping(value = "/tables", method = GET)
    public RestResult getTables() {
        try {
            List<TableInfo> tables = sysDBDao.getTableInfo();
            return new RestResult(ResultType.OK, om.writeValueAsString(tables));
        }
        catch (Exception ex) {
            logger.fatal("get tables error: " + ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }

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
            logger.fatal("get table error: " + ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }

    @RequestMapping(value = "/interfaces/{dbName}/{tableName}", method = GET)
    public RestResult getInterfacesBy(
            @PathVariable("dbName")    String dbName,
            @PathVariable("tableName") String tableName
    ) {
        try {
            List<InterfaceInfo> apis = sysDBDao.getInterfaceInfoBy(dbName, tableName);
            return new RestResult(ResultType.OK, om.writeValueAsString(apis));
        }
        catch (Exception ex) {
            logger.fatal("get interfaces error: " + ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }

    @RequestMapping(value = "/interface/{id}", method = GET)
    public RestResult getInterfaceById(
            @PathVariable("id") int id
    ) {
        try {
            InterfaceInfo api = sysDBDao.getInterfaceInfoById(id);
            return new RestResult(ResultType.OK, om.writeValueAsString(api));
        }
        catch (Exception ex) {
            logger.fatal("get interface by id error: " + ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }

    @RequestMapping(value = "/interface/{id}/acl", method = GET)
    public RestResult getInterfaceAclList(
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
            logger.fatal("grant interface error: " + ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }

    @RequestMapping(value = "/interface/{id}/revoke/{clientName}", method = DELETE)
    public RestResult revokeInterfaceFromClient(
            @PathVariable("id") int id,
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
            logger.fatal("grant interface error: " + ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }

    @RequestMapping(value = "/acl/dump", method = GET)
    public RestResult dumpAclCache() {
        try {
            List<AclInfo> aclInfoList = aclCacheService.dumpAllAcl();
            return new RestResult(ResultType.OK, om.writeValueAsString(aclInfoList));
        }
        catch (Exception ex) {
            logger.fatal("dump acl cache error: " + ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }

}
