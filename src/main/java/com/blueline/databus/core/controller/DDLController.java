package com.blueline.databus.core.controller;

import javax.servlet.http.HttpServletRequest;

import com.blueline.databus.core.helper.SQLParser;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static org.springframework.web.bind.annotation.RequestMethod.*;

import com.blueline.databus.core.bean.RestResult;
import com.blueline.databus.core.bean.ResultType;
import com.blueline.databus.core.helper.DBHelper;

@CrossOrigin
@RestController
@RequestMapping("/api/db")
public class DDLController{
    private final Logger logger = Logger.getLogger(DDLController.class);
    
    @Autowired
    private HttpServletRequest request;

    @Autowired
    private DBHelper dbHelper;

    /**
     * 建表
     * POST /api/db/{db_name}
     * @param dbName
     * @return
     */
    @RequestMapping(value = "/{dbName}", method = POST)
    public RestResult createDbTable(
            @PathVariable("dbName") String dbName,
            @RequestBody String jsonBody
    ) {
        try {
            dbHelper.createTable(dbName, jsonBody);
            
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
