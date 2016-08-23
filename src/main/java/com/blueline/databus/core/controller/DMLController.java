package com.blueline.databus.core.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.blueline.databus.core.dao.CoreDBDao;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;

import com.blueline.databus.core.datatype.RestResult;
import com.blueline.databus.core.datatype.ResultType;

/**
 * 处理数据CRUD相关的操作(DML)
 */
@RestController
@RequestMapping("/api/data")
public class DMLController {
    private final Logger logger = Logger.getLogger(DMLController.class);

    @Autowired
    private HttpServletResponse response;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private CoreDBDao coreDBDao;

    @RequestMapping(value = "/{dbName}/{tableName}", method = GET)
    public RestResult queryData(
        @PathVariable("dbName")    String dbName,
        @PathVariable("tableName") String tableName
    ) {
        try {
            String jsonData = coreDBDao.queryData(dbName, tableName, request.getParameterMap());
            response.setHeader("Cache-Control", "public");
            response.setHeader("Cache-Control", "must-revalidate");
            response.setHeader("Cache-Control", "max-age=3600");
            return new RestResult(ResultType.OK, jsonData);
        }
        catch (Exception ex) {
            logger.fatal(ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }

    @RequestMapping(value = "/{dbName}/{tableName}", method = DELETE)
    public RestResult deleteData(
        @PathVariable("dbName")    String dbName,
        @PathVariable("tableName") String tableName
    ) {
        try {
            int count = coreDBDao.deleteData(dbName, tableName, request.getParameterMap());
            if (count > 0) {
                return new RestResult(ResultType.OK, String.format("%d rows deleted", count));
            }
            else{
                return new RestResult(ResultType.FAIL, "nothing deleted");
            }
        }
        catch (Exception ex) {
            logger.fatal(ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }

    @RequestMapping(value = "/{dbName}/{tableName}", method = POST)
    public RestResult insertData(
        @PathVariable("dbName")    String dbName,
        @PathVariable("tableName") String tableName,
        @RequestBody String jsonBody
    ) {
        try {
            int count = coreDBDao.insertData(dbName, tableName, jsonBody);
            if (count > 0) {
                return new RestResult(ResultType.OK, String.format("%s rows inserted", count));
            }
            else {
                return new RestResult(ResultType.FAIL, "nothing inserted");
            }
        }
        catch (Exception ex) {
            logger.fatal(ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }

    @RequestMapping(value = "/{dbName}/{tableName}/{colName}/{colValue}", method = PUT)
    public RestResult updateData(
        @PathVariable(value = "dbName")    String dbName,
        @PathVariable("tableName") String tableName,
        @PathVariable("colName")   String colName,
        @PathVariable("colValue")  String colValue,
        @RequestBody String jsonBody
    ) {
        try {
            int count = coreDBDao.updateData(dbName, tableName, colName, colValue, jsonBody);
            if (count > 0) {
                return new RestResult(ResultType.OK, String.format("%s rows updated", count));
            }
            else {
                return new RestResult(ResultType.FAIL,"nothing updated");
            }
        }
        catch (Exception ex) {
            logger.fatal(ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }
}
