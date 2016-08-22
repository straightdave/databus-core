package com.blueline.databus.core.controller;

import com.blueline.databus.core.dao.CoreDBDao;
import com.blueline.databus.core.dao.SysDBDao;
import com.blueline.databus.core.exception.InternalException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import static org.springframework.web.bind.annotation.RequestMethod.*;

import com.blueline.databus.core.datatype.RestResult;
import com.blueline.databus.core.datatype.ResultType;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/db")
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
            @PathVariable("dbName") String dbName,
            @PathVariable("tableName") String tableName
    ) {

        try {
            // read request body
            BufferedReader r = request.getReader();
            String body = r.lines().collect(Collectors.joining(System.lineSeparator()));

            if (StringUtils.isEmpty(body)) {
                throw new InternalException("got no request body");
            }

            String appkey = request.getHeader("x-appkey");

            coreDBDao.createTable(dbName, tableName, body);
        }
        catch (Exception ex) {
            logger.error(ex.getMessage());
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
            return new RestResult(ResultType.FAIL, "drop table failed: " + ex.getMessage());
        }

        return new RestResult(ResultType.OK, "Table Dropped");
    }
}
