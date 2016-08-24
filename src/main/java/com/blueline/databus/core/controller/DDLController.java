package com.blueline.databus.core.controller;

import com.blueline.databus.core.dao.CoreDBDao;
import com.blueline.databus.core.dao.SysDBDao;
import com.blueline.databus.core.exception.InternalException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import static org.springframework.web.bind.annotation.RequestMethod.*;

import com.blueline.databus.core.datatype.RestResult;
import com.blueline.databus.core.datatype.ResultType;

/**
 * 定义数据(DDL)相关的服务接口
 */
@RestController
@RequestMapping("/api/def")
public class DDLController{
    private final Logger logger = Logger.getLogger(DDLController.class);

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private CoreDBDao coreDBDao;

    @Autowired
    private SysDBDao sysDBDao;

    @RequestMapping(value = "/{dbName}/{tableName}", method = POST)
    public RestResult createTable(
            @PathVariable("dbName")    String dbName,
            @PathVariable("tableName") String tableName,
            @RequestBody String jsonBody
    ) {
        try {
            coreDBDao.createTable(dbName, tableName, jsonBody);
        }
        catch (Exception ex) {
            logger.fatal("table creating failed: " + ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }

        try {
            String clientId = request.getHeader("x-ownerid");
            if (StringUtils.isEmpty(clientId)) {
                // 调用此api的人(管理员)没有传递建表人的id
                throw new InternalException("header:x-ownerid is not provided.");
            }
            int owner_id = Integer.valueOf(clientId);
            sysDBDao.doAfterTableCreated(dbName, tableName, owner_id);
        }
        catch (Exception ex) {
            logger.fatal("Post-Creation action failed, rollback table creation: " + ex.getMessage());
            coreDBDao.dropTableIfExist(dbName, tableName);
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
        return new RestResult(ResultType.OK, "table created");
    }

    @RequestMapping(value = "/{dbName}/{tableName}", method = DELETE)
    public RestResult dropTable(
            @PathVariable("dbName") String dbName,
            @PathVariable("tableName") String tableName
    ) {
        try {
            coreDBDao.dropTable(dbName, tableName);
        }
        catch (DataAccessException ex) {
            logger.fatal(ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }

        try {
            sysDBDao.doAfterTableDropped(dbName, tableName);
        }
        catch (Exception ex) {
            logger.fatal("Post-Dropping action failed, but do not roll back table dropping");
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
        return new RestResult(ResultType.OK, "Table Dropped");
    }
}
