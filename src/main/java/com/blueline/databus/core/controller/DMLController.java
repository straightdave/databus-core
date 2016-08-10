package com.blueline.databus.core.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static org.springframework.web.bind.annotation.RequestMethod.*;

import com.blueline.databus.core.bean.RestResult;
import com.blueline.databus.core.bean.ResultType;
import com.blueline.databus.core.helper.DBHelper;

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
    private DBHelper dbHelper;

    @RequestMapping(value = "/{dbName}/{realTableName}", method = GET)
    public RestResult queryData(
        @PathVariable("dbName") String dbName,
        @PathVariable("realTableName") String realTableName
    ) {
        try {
            String jsonData = dbHelper.queryData(dbName, realTableName, request.getParameterMap());

            // 设置缓存相关参数
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

    @RequestMapping(value = "/{dbName}/{realTableName}", method = DELETE)
    public RestResult deleteData(
        @PathVariable("dbName") String dbName,
        @PathVariable("realTableName") String realTableName
    ) {
        try {
            int count = dbHelper.deleteData(dbName, realTableName, request.getParameterMap());
            if (count > 0) {
                return new RestResult(ResultType.OK, String.format("%d rows deleted", count));
            }
            else{
                return new RestResult(ResultType.FAIL, "nothing deleted");
            }
        }
        catch (Exception ex) {
            logger.info(ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }

    @RequestMapping(value = "/{dbName}/{realTableName}", method = POST)
    public RestResult insertData(
        @PathVariable("dbName") String dbName,
        @PathVariable("realTableName") String realTableName,
        @RequestBody String jsonBody
    ) {
        try {
            int count = dbHelper.insertData(dbName, realTableName, jsonBody);
            if (count > 0) {
                return new RestResult(ResultType.OK, String.format("%s rows inserted by %s", count));
            } else {
                return new RestResult(ResultType.FAIL, "nothing inserted");
            }
        }
        catch (Exception ex) {
            logger.info(ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }

    @RequestMapping(value = "/{dbName}/{realTableName}/{colName}/{colValue}", method = PUT)
    public RestResult updateData(
        @PathVariable("dbName") String dbName,
        @PathVariable("realTableName") String realTableName,
        @PathVariable("colName") String colName,
        @PathVariable("colValue") String colValue
    ) {
        try {
            int count = dbHelper.updateData(dbName,realTableName, colName, colValue, request.getParameterMap());
            if (count > 0) {
                return new RestResult(ResultType.OK, String.format("%s rows updated by %s", count));
            } else {
                return new RestResult(ResultType.FAIL,"nothing updated");
            }
        } catch (Exception ex) {
            logger.info(ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }
}
