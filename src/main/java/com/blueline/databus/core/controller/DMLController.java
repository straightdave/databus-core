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

import java.util.List;
import java.util.Map;

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

    /**
     * 数据查询
     *
     * <pre>
     *     <code>GET /api/data/{dbName}/{tableName}[?args...]</code>
     * </pre>
     *
     * <p>参数说明</p>
     *
     * <strong>meta参数</strong>:注意名称带有下划线;这些条件都有默认值;
     * <ul>
     *     <li><strong>_by</strong>:排序依据的列名;如果不写,默认是'id'列</li>
     *     <li><strong>_order</strong>:排序方向;取值为"asc"/"desc";默认是升序"asc"</li>
     *     <li><strong>_skip</strong>:跳过条目数;默认是0</li>
     *     <li><strong>_take</strong>:取值条目数;默认是10(默认值可配置)</li>
     * </ul>
     *
     * <strong>条件参数</strong>
     * <ul>
     *     <li>
     *         <strong>{col_name}={value}</strong>:列名直接作为条件,如:
     *         <pre><code>?id=14&amp;name=dave</code></pre>
     *     </li>
     *     <li>
     *         相同列名作为条件可以重复使用,结果取交集;如:
     *         <pre><code>?id=1&amp;id=5</code></pre>
     *     </li>
     *     <li>
     *         <strong>{col_name}_start或{col_name}_begin或{col_name}_gt</strong>列大于等于某值,如:
     *         <pre><code>?age_start=9&amp;age_stop=60</code></pre>
     *     </li>
     *     <li>
     *         <strong>{col_name}_stop或{col_name}_end或{col_name}_lt</strong>列小于等于某值,如:
     *         <pre><code>?age_start=9&amp;age_stop=60</code></pre>
     *     </li>
     *     <li>
     *         <strong>{col_name}_not</strong>:列不等于某值,如:
     *         <pre><code>?name_not=dave</code></pre>
     *     </li>
     *     <li>
     *         相同列可重复添加不等于某值条件,结果取并集;如:
     *         <pre><code>?name_not=dave&amp;name_not=michael</code></pre>
     *     </li>
     * </ul>
     *
     * @param dbName 数据库名
     * @param tableName 表名
     * @return json格式的数据
     * @see com.blueline.databus.core.helper.SQLParser#parseSQL4Select(Map)
     */
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

    /**
     * 删除表数据
     * <pre>
     *     <code>DELETE /api/data/{dbName}/{tableName}[?args...]</code>
     * </pre>
     *
     * <strong>条件参数</strong>
     * <ul>
     *     <li>
     *         <strong>{col_name}={value}</strong>:列名直接作为条件,如:
     *         <pre><code>?id=14&amp;name=dave</code></pre>
     *     </li>
     *     <li>
     *         相同列名作为条件可以重复使用,结果取交集;如:
     *         <pre><code>?id=1&amp;id=5</code></pre>
     *     </li>
     *     <li>
     *         <strong>{col_name}_start或{col_name}_begin或{col_name}_gt</strong>列大于等于某值,如:
     *         <pre><code>?age_start=9&amp;age_stop=60</code></pre>
     *     </li>
     *     <li>
     *         <strong>{col_name}_stop或{col_name}_end或{col_name}_lt</strong>列小于等于某值,如:
     *         <pre><code>?age_start=9&amp;age_stop=60</code></pre>
     *     </li>
     *     <li>
     *         <strong>{col_name}_not</strong>:列不等于某值,如:
     *         <pre><code>?name_not=dave</code></pre>
     *     </li>
     *     <li>
     *         相同列可重复添加不等于某值条件,结果取并集;如:
     *         <pre><code>?name_not=dave&amp;name_not=michael</code></pre>
     *     </li>
     * </ul>
     *
     * <p><strong>注意</strong>: 如果没有附加参数,为保护数据安全,不删除数据</p>
     *
     * @param dbName 数据库名
     * @param tableName 表名
     * @return 结果信息
     * @see com.blueline.databus.core.helper.SQLParser#parseSQL4Delete(Map)
     */
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

    /**
     * 插入数据
     * <pre>
     *     <code>POST /api/data/{dbName}/{tableName}</code>
     * </pre>
     *
     * 请求体中是json格式的参数;
     * json是个列表,每个元素代表要插入的一行数值;如:
     *
     * <pre>
     *      <code>
     *           [
     *               {"name":"dave", "age":"18", "sex":"male"},
     *               {"name":"mike", "age":"18", "sex":"male", "salary":"20k"},
     *               ...
     *           ]
     *      </code>
     * </pre>
     * <ul>
     *     <li>提供的值中,如果缺少某些列,取'DEFAULT'字符作为值(交给数据库引擎处理)</li>
     *     <li>忽略id列和数据表中没有的列的值</li>
     * </ul>
     *
     * @param dbName 数据库名
     * @param tableName 表名
     * @param jsonBody 请求体参数
     * @return 结果信息
     * @see com.blueline.databus.core.helper.SQLParser#parseSQL4Insert(String, List)
     */
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

    /**
     * 更新数据
     * <pre>
     *     <code>PUT /api/data/{dbName}/{tableName}/{colName}/{colValue}</code>
     * </pre>
     *
     * 更新符合查找条件的数据。条件就是后两个路径参数colName和colValue;
     * 这两个参数组成UPDATE语句中的 <pre><code>WHERE `colName` = 'colValue'</code></pre> 条件句;
     * 修改信息是在body中的json对象列表,形式大概像insert所需类似:
     *
     * <pre><code> [{"name":"dave", "age":"18", "sex":"male"}] </code></pre>
     *
     * 只不过,只有列表第一个json对象元素会拿来使用;
     * 它组成UPDATE语句中的<pre><code>SET `name` = 'dave', `age` = '18'</code></pre>部分;
     *
     * <p>记住,目前不过滤不存在的字段。如果条件和修改信息中有不存在的字段,都会导致最终的错误返回。</p>
     *
     * @param dbName 数据库名
     * @param tableName 表名
     * @param colName 列名
     * @param colValue 列值(作为查询条件)
     * @param jsonBody 请求体中,json格式的参数
     * @return 结果信息
     * @see com.blueline.databus.core.helper.SQLParser#parseSQL4Update(String, String, String)
     */
    @RequestMapping(value = "/{dbName}/{tableName}/{colName}/{colValue}", method = PUT)
    public RestResult updateData(
        @PathVariable("dbName")    String dbName,
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
