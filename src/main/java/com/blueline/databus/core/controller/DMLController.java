package com.blueline.databus.core.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static org.springframework.web.bind.annotation.RequestMethod.*;

import com.blueline.databus.core.bean.RestResult;
import com.blueline.databus.core.bean.ResultType;
import com.blueline.databus.core.bean.DBHelper;

/**
 * 处理所有数据CRUD相关的操作(DML)
 */
@CrossOrigin                    /* 应用默认的CORS配置,可以跨域访问 */
@RestController                 /* 涵盖了Spring MVC中的@ResponseBody和@Controller */
@RequestMapping("/data")
public class DMLController {
    private final Logger logger = Logger.getLogger(DMLController.class);

    @Autowired
    private HttpServletResponse response;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private DBHelper dbHelper;

    /**
     * 查询数据表的数据
     * @param dbName 数据库名
     * @param realTableName 带有用户名前缀的表名
     * @return RestResult
     */
    @RequestMapping(value = "/{dbName}/{realTableName}", method = GET)
    public RestResult query(
        @PathVariable("dbName") String dbName,
        @PathVariable("realTableName") String realTableName
    ) {
        try {
            String jsonData = dbHelper.getData(dbName, realTableName, request.getParameterMap());

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

    /*
    @RequestMapping(value = "/{dbName}/{realTableName}", method = DELETE)
    public RestResult delete(
        @PathVariable("dbName") String dbName,
        @PathVariable("realTableName") String realTableName
    ) {
        DBHelper dbHelper = DBHelper.getInstance();
        try {
            int count = dbHelper.deleteData(dbName, realTableName, request.getQueryString());
            if (count > 0) {
                return new RestResult(ResultType.OK, "delete success!!!");
            }
            else{
                return new RestResult(ResultType.FAIL, "delete failed!!!");
            }
        }
        catch (SQLException ex) {
            logger.info(ex.getMessage());
            return new RestResult(ResultType.FAIL, ex.getMessage());
        }
        catch (Exception ex) {
            logger.info(ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
    }


    @RequestMapping(value = "/{dbName}/{realTableName}/insert", method = POST)
    public RestResult insert(
        @PathVariable("dbName") String dbName,
        @PathVariable("realTableName") String realTableName
    ) {
        DBHelper dbHelper = DBHelper.getInstance();
        String body = request.getSession().getAttribute("body").toString();

        try {
            int count = dbHelper.insertData(dbName, realTableName , body);
            if (count > 0) {
                logger.info( " insert success ");
                return new RestResult(ResultType.OK, "insert success!!!");
            } else {
                logger.info(  " insert failed ");
                return new RestResult(ResultType.FAIL,"insert failed!!!");
            }
        } catch (Exception ex) {
            logger.info( ex.getMessage());
            return new RestResult(ResultType.ERROR,ex.getMessage());
        }
    }

    @RequestMapping(value = "/{dbName}/{realTableName}/update", method = POST)
    public RestResult update(
        @PathVariable("dbName") String dbName,
        @PathVariable("realTableName") String realTableName
    ) {
        DBHelper dbHelper = DBHelper.getInstance();

        try {
            String body = request.getSession().getAttribute("body").toString();
            int count = dbHelper.updateData(dbName,realTableName, body);
            if (count > 0) {
                logger.info(" update success ");
                return new RestResult(ResultType.OK, "update success!!!");
            } else {
                logger.info(" update failed ");
                return new RestResult(ResultType.FAIL,"update failed!!!");
            }
        } catch (SQLException ex) {
            logger.info(new Date() + "——" + ex.getMessage());
            return new RestResult(ResultType.FAIL,ex.getMessage());
        }
    }
    */


}
