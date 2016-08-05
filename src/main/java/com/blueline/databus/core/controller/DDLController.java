package com.blueline.databus.core.controller;

import javax.servlet.http.HttpServletRequest;

import com.blueline.databus.core.bean.SQLParser;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static org.springframework.web.bind.annotation.RequestMethod.*;

import com.blueline.databus.core.bean.RestResult;
import com.blueline.databus.core.bean.ResultType;
import com.blueline.databus.core.bean.DBHelper;

@CrossOrigin
@RestController
@RequestMapping("/db")
public class DDLController{
    private final Logger logger = Logger.getLogger(DDLController.class);
    
    @Autowired
    private HttpServletRequest request;

    @Autowired
    private SQLParser sqlParser;

    @Autowired
    private DBHelper dbHelper;

    /**
     * 建表
     * POST /db/{db_name}/table
     * @param dbName
     * @return
     */
    @RequestMapping(value = "/{dbName}/table", method = POST)
    public RestResult createDbTable(
            @PathVariable("dbName") String dbName,
            @RequestBody String jsonBody
    ) {

        try {
            String sql = sqlParser.parseQueryString4CreateTable(jsonBody);
            dbHelper.createDbTable(dbName, sql);
            
            // create 4 interfaces automatically
            // as well as Accessibility for this user

        }
        catch (Exception ex) {
            logger.error(ex.getMessage());
            return new RestResult(ResultType.ERROR, ex.getMessage());
        }
        return new RestResult(ResultType.OK, "Table Created");
    }
}
