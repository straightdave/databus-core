package com.blueline.databus.core.controller;

import com.blueline.databus.core.dao.CoreDBDao;
import com.blueline.databus.core.dao.SysDBDao;
import com.blueline.databus.core.exception.InternalException;
import com.blueline.databus.core.datatype.RestResult;
import com.blueline.databus.core.datatype.ResultType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import static org.springframework.web.bind.annotation.RequestMethod.*;

/**
 * 定义数据表(DDL)相关的服务接口
 */
@RestController
@RequestMapping("/api/def")
public class DDLController {
    private final Logger logger = Logger.getLogger(DDLController.class);

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private CoreDBDao coreDBDao;

    @Autowired
    private SysDBDao sysDBDao;

    /**
     * 创建数据表(并自动创建相关sys数据);
     * 本接口通常是由admin调用,客户端一般不会直接调用;
     * 因此admin调用时需要加上"x-ownerid"请求头,指出是谁创建的表;
     * <pre>
     *     <code>POST /api/def/{dbName}/{tableName}</code>
     * </pre>
     * <p>
     *     请求体:json列表,列表每个元素描述列信息,如:
     * </p>
     * <p>
     *     [{"name":"col_name", "type":"col_type", "nullable":"true/false", "unique":"true/false","index":"true/false"},
     *     {}, ...]
     * </p>
     * <ul>
     *     <li><code>name</code>:列名;建议为小写单词与下划线的组合</li>
     *     <li><code>type</code>:列数据类型;如"int","int unsigned","varchar(50)"等</li>
     *     <li><code>nullable</code>:可选;如果存在且为"true",则值允许NULL</li>
     *     <li><code>unique</code>:可选;如果存在且为"true",则该列添加UNIQUE KEY</li>
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
        // create the table in core database
        try {
            coreDBDao.createTable(dbName, tableName, jsonBody);
        }
        catch (Exception ex) {
            String err = String.format("create table {%s.%s} failed with error: %s",
                    dbName, tableName, ex.getMessage());
            logger.fatal(err);
            return new RestResult(ResultType.ERROR, err);
        }

        // create meta info of the table in sys database
        try {
            String clientId = request.getHeader("x-ownerid");
            if (StringUtils.isEmpty(clientId)) {
                throw new InternalException("header:x-ownerid should be provided to indicate the table owner.");
            }
            int owner_id = Integer.valueOf(clientId);
            sysDBDao.doAfterTableCreated(dbName, tableName, owner_id);
        }
        catch (Exception ex) {
            String err = String.format("create meta info for {%s.%s} failed with error: %s",
                    dbName, tableName, ex.getMessage());
            coreDBDao.dropTableIfExist(dbName, tableName);
            logger.fatal(err);
            return new RestResult(ResultType.ERROR, err);
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
}
