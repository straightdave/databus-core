package com.blueline.databus.core.filter;

import com.blueline.databus.core.dao.ApiRecordService;
import com.blueline.databus.core.datatype.*;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.blueline.databus.core.helper.FilterResponseRender;
import com.blueline.databus.core.dao.SysDBDao;
import com.blueline.databus.core.helper.MACHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

/**
 *  本filter处于实际访问检查的最外层,过滤不合法的API请求;
 *  即检查请求的头部,计算MAC;
 */
public class AuthenticationFilter implements Filter {

    @Value("${admin.appkey}")
    private String adminAppKey;

    @Value("${admin.skey}")
    private String adminSKey;

    @Autowired
    private ApiRecordService apiRecordService;

    @Autowired
    private SysDBDao sysDBDao;

    /**
     * 从请求中获取的数据:
     * <ul>
     *     <li>请求路径(包含query string)</li>
     *     <li>HTTP方法</li>
     *     <li>头部"x-appkey"</li>
     *     <li>头部"x-mac"</li>
     * </ul>
     *
     * <ol>
     *     <li>首先判断必要的请求头部是否存在</li>
     *     <li>使用头部的提供的appkey在数据库中查询对应的skey</li>
     *     <li>计算MAC值:将{appkey}_{HTTP方法}_{url}作为payload,和查询出的skey组合,并计算MAC</li>
     *     <li>比较计算出的MAC和请求头部x-mac获取的值进行比较</li>
     * </ol>
     *
     * @param req Servlet请求
     * @param resp Servlet返回
     * @param chain Filter链
     * @throws IOException 可能抛出IOException
     * @throws ServletException 可能抛出ServletException
     * @see MACHelper#calculateMAC(String, String)
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)resp;

        // 记录API调用,不加 Query String
        // 这个指令,如果redis没有ready,仅忽略
        String apiKey = String.format("%s %s", request.getMethod().toUpperCase(), request.getRequestURI());
        apiRecordService.recordAPICall(apiKey);

        // header 名称大小写无关
        String mac    = request.getHeader("x-mac");
        String appKey = request.getHeader("x-appkey");

        if (StringUtils.isEmpty(appKey)) {
            RestResult result = new RestResult(ResultType.FAIL, "header x-appkey missed");
            FilterResponseRender.render(response, result);
            return;
        }

        if (StringUtils.isEmpty(mac)) {
            RestResult result = new RestResult(ResultType.FAIL, "header x-mac missed");
            FilterResponseRender.render(response, result);
            return;
        }

        String skey;
        if (appKey.equals(adminAppKey)) {
            skey = adminSKey;
        }
        else {
            skey = sysDBDao.getClientByAppKey(appKey).getSKey();
        }

        if (StringUtils.isEmpty(skey)) {
            RestResult result = new RestResult(ResultType.FAIL, "get no secure_key by app_key: " + appKey);
            FilterResponseRender.render(response, result);
            return;
        }

        // 计算MAC
        // payload是 {app key}+{method}+{uri(?query string)}
        String payload = String.format("%s_%s_%s",
                appKey,
                request.getMethod().toUpperCase(),
                request.getRequestURI() +
                (request.getQueryString() != null ? "?" + request.getQueryString() : ""));

        String calculatedMAC = MACHelper.calculateMAC(skey, payload);
        if (!mac.equals(calculatedMAC)) {
            RestResult result = new RestResult(ResultType.FAIL,
                    String.format("MAC not match. Expect: %s, actual: %s", calculatedMAC, mac));
            FilterResponseRender.render(response, result);
            return;
        }
        chain.doFilter(req, resp);
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {}

    @Override
    public void destroy() {}
}
