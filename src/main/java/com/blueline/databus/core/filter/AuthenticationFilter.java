package com.blueline.databus.core.filter;

import com.blueline.databus.core.configtype.AdminConfig;
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
import com.blueline.databus.core.helper.RedisHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

/**
 *  本filter在最外层,过滤不合法的API请求
 *  即检查请求的header,计算MAC等
 */
public class AuthenticationFilter implements Filter {

    @Autowired
    private AdminConfig adminConfig;

    @Autowired
    private RedisHelper redisHelper;

    @Autowired
    private SysDBDao sysDBDao;

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)resp;

        // 记录API调用,不加 Query String
        // 这个指令,如果redis没有ready,仅忽略
        redisHelper.recordAPICall(request.getRequestURI());

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
        if (appKey.equals(adminConfig.getAppkey())) {
            skey = adminConfig.getSkey();
        }
        else {
            skey = sysDBDao.getSKey(appKey);
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
