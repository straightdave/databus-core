package com.blueline.databus.core.controller;

import com.blueline.databus.core.dao.SysDBDao;
import com.blueline.databus.core.datatype.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.ResultSet;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/api/sys")
public class AdminController {
    private final Logger logger = Logger.getLogger(AdminController.class);

    @Value("${admin.appkey}")
    private String adminAppKey;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private SysDBDao sysDBDao;

    @RequestMapping(value = "/clients", method = GET)
    public RestResult getClients() {
        try {
            ObjectMapper om = new ObjectMapper();
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
            ObjectMapper om = new ObjectMapper();
            ClientInfo client = sysDBDao.getClientBy(name);
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
            ObjectMapper om = new ObjectMapper();
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
            ObjectMapper om = new ObjectMapper();
            return new RestResult(ResultType.OK, om.writeValueAsString(table));
        }
        catch (Exception ex) {
            logger.fatal("get table error: " + ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }

    @RequestMapping(value = "/interfaces/{dbName}/{tableName}", method = GET)
    public RestResult getInterfacesBy(
            @PathVariable("dbName") String dbName,
            @PathVariable("tableName") String tableName
    ) {
        try {
            List<InterfaceInfo> apis = sysDBDao.getInterfaceInfoBy(dbName, tableName);
            ObjectMapper om = new ObjectMapper();
            return new RestResult(ResultType.OK, om.writeValueAsString(apis));
        }
        catch (Exception ex) {
            logger.fatal("get interfaces error: " + ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }

}
