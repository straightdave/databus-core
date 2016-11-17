package com.blueline.databus.core.controller;

import com.blueline.databus.core.dao.CoreDBDao;
import com.blueline.databus.core.dao.SysDBDao;
import com.blueline.databus.core.datatype.ColumnInfo;
import com.blueline.databus.core.datatype.RestResult;
import com.blueline.databus.core.datatype.ResultType;

import com.blueline.databus.core.exception.InternalException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * 定义数据表(DDL)相关的服务接口
 */
@RestController
@RequestMapping("/api/def")
public class DDLController {
    private final Logger logger = Logger.getLogger(DDLController.class);

    @Value("${admin.appkey}")
    private String adminAppKey;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private CoreDBDao coreDBDao;

    @Autowired
    private SysDBDao sysDBDao;

    /**
     * 创建数据表(并自动创建相关sys数据);
     * <p>
     *     本接口通常是由admin调用,客户端一般不会直接调用
     * </p>
     * <pre>
     *     <code>POST /api/def/{dbName}/{tableName}</code>
     * </pre>
     * <p>
     *     请求体:json格式如下,包含表的一些信息和列信息列表，其中每个元素描述某一列的信息,如:
     * </p>
     * <pre>
     *     <code>
     *         {
     *           "owner_id"    : "10",
     *           "owner_name"  : "crm",  // owner_id和owner_name可以任选其一，如若同时存在，以id为准
     *           "description" : "this is a db table for client no.10",
     *           "columns"     : [
     *                             {
     *                               "name"        : "col_name",
     *                               "data_type"   : "col_type",
     *                               "data_length" : "50",
     *                               "nullable"    : true/false,
     *                               "unique"      : true/false,
     *                               "index"       : true/false
     *                             },
     *                             {}, ...
     *                          ]
     *         }
     *     </code>
     * </pre>
     *
     * 关于列信息：
     * <ul>
     *     <li><code>name</code>:列名;建议为小写单词与下划线的组合</li>
     *     <li><code>data_type</code>:列数据类型;如"int","int unsigned","varchar(50)"等</li>
     *     <li><code>data_length</code>:列数据长度：目前只对char、varchar类型有效；默认255</li>
     *     <li><code>nullable</code>:可选;如果存在且为true,则值允许NULL</li>
     *     <li><code>unique</code>:可选;如果存在且为true,则该列添加UNIQUE KEY</li>
     *     <li><code>index</code>:可选;如果存在且为true",则为该列添加索引;如果unique为true则忽略此项</li>
     * </ul>
     *
     * @param dbName 数据库名
     * @param tableName 表名
     * @param jsonBody 请求体json参数
     * @return 结果信息字符串
     */
    @RequestMapping(value = "/{dbName}/{tableName}", method = POST)
    public RestResult createTable(
            @PathVariable("dbName")    String dbName,
            @PathVariable("tableName") String tableName,
            @RequestBody               String jsonBody
    ) {
        Map<String, Object> params = null;
        String owner_id = null, owner_name = null;
        try {
            if (!request.getHeader("x-appkey").equalsIgnoreCase(adminAppKey)) {
                return new RestResult(ResultType.FAIL, "admin only");
            }

            ObjectMapper om = new ObjectMapper();
            params = om.readValue(jsonBody, Map.class);

            if (params == null || params.size() < 2) {
                return new RestResult(ResultType.ERROR, "json body parsing failed or lack of necessary fields");
            }

            if (!params.keySet().contains("columns")) {
                return new RestResult(ResultType.ERROR, "column info not provided");
            }

            if (params.keySet().contains("owner_id")) {
                owner_id = params.get("owner_id").toString();
            }

            if (params.keySet().contains("owner_name")) {
                owner_name = params.get("owner_name").toString();
            }

            if (StringUtils.isEmpty(owner_id) && StringUtils.isEmpty(owner_name)) {
                return new RestResult(ResultType.ERROR, "neither owner_id nor owner_name provided");
            }

            ArrayList<Map<String, Object>> list = ((ArrayList<Map<String, Object>>) params.get("columns"));

            if (list == null || list.size() < 1) {
                throw new InternalException("columns field in json body parsing failed");
            }

            coreDBDao.createTable(dbName, tableName, list);
        }
        catch (Exception ex) {
            String columns_info = params.get("columns").toString();
            String err = String.format(
                    "create table {%s.%s} failed with error: %s; columns: %s",
                    dbName, tableName, ex.getMessage(), columns_info);
            logger.fatal(err);
            return new RestResult(ResultType.ERROR, err);
        }

        // create meta info of the table in sys database
        try {
            if (!StringUtils.isEmpty(owner_id)) {
                int ownerId = Integer.valueOf(owner_id);
                sysDBDao.doAfterTableCreated(dbName, tableName, ownerId, params.get("description").toString());
            }
            else {
                sysDBDao.doAfterTableCreated(dbName, tableName, owner_name, params.get("description").toString());
            }
        }
        catch (Exception ex) {
            String err = String.format("create meta info for {%s.%s} failed with error: %s",
                    dbName, tableName, ex.getMessage());
            coreDBDao.dropTableIfExist(dbName, tableName);
            logger.fatal(err);
            return new RestResult(ResultType.ERROR, err + ";Roll-backed");
        }
        return new RestResult(ResultType.OK, String.format("table {%s.%s} created", dbName, tableName));
    }

    /**
     * 删除某表(并自动清除sys表中关联meta数据)
     * <pre>
     *     <code>DELETE /api/def/{dbName}/{tableName}</code>
     * </pre>
     *
     * @param dbName 数据库名
     * @param tableName 表名
     * @return 结果信息
     */
    @RequestMapping(value = "/{dbName}/{tableName}", method = DELETE)
    public RestResult dropTable(
            @PathVariable("dbName")    String dbName,
            @PathVariable("tableName") String tableName
    ) {
        try {
            if (!request.getHeader("x-appkey").equalsIgnoreCase(adminAppKey)) {
                return new RestResult(ResultType.FAIL, "admin only");
            }
            coreDBDao.dropTable(dbName, tableName);
        }
        catch (DataAccessException ex) {
            String err = String.format("drop table {%s.%s} failed with error: %s", dbName, tableName, ex.getMessage());
            logger.fatal(err);
            return new RestResult(ResultType.ERROR, err);
        }

        try {
            sysDBDao.doAfterTableDropped(dbName, tableName);
        }
        catch (Exception ex) {
            String err = String.format("drop meta info of the table {%s.%s} failed with error: %s",
                    dbName, tableName, ex.getMessage());
            logger.fatal(err);
            return new RestResult(ResultType.ERROR, err);
        }
        return new RestResult(ResultType.OK, String.format("table {%s.%s} dropped", dbName, tableName));
    }

    /**
     * 获取表的列信息（通过数据库元信息查询获取）
     * @param dbName 数据库名
     * @param tableName 数据表名
     * @return 返回数据message中包含该表的列信息json格式（json列表）
     */
    @RequestMapping(value = "/{dbName}/{tableName}/columns", method = GET)
    public RestResult getColumnInfo(
            @PathVariable("dbName")    String dbName,
            @PathVariable("tableName") String tableName
    ) {
        try {
            List<ColumnInfo> columns = coreDBDao.getColumns(dbName, tableName);
            return new RestResult(ResultType.OK, new ObjectMapper().writeValueAsString(columns));
        }
        catch (Exception ex) {
            String err = String.format(
                    "get column meta info of the table {%s.%s} failed with error: %s",
                    dbName, tableName, ex.getMessage());
            logger.fatal(err);
            return new RestResult(ResultType.ERROR, err);
        }
    }
}
