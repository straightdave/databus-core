package com.blueline.databus.core.filter;

import com.blueline.databus.core.config.AdminConfig;
import com.blueline.databus.core.bean.*;

import java.io.IOException;
import java.util.stream.Collectors;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)resp;

        // 记录API调用
        redisHelper.recordAPICall(request.getServletPath());

        String mac    = request.getHeader("x-mac");
        String appKey = request.getHeader("x-appkey");

        if (StringUtils.isEmpty(mac)) {
            RestResult result = new RestResult(ResultType.FAIL, "header: x-appkey missed");
            FilterResponseRender.render(response, result);
            return;
        }
        if (StringUtils.isEmpty(appKey)) {
            RestResult result = new RestResult(ResultType.FAIL, "header: x-mac missed");
            FilterResponseRender.render(response, result);
            return;
        }

        String skey;
        if (appKey.equals(adminConfig.getAppkey())) {
            skey = adminConfig.getSkey();
        }
        else {
            skey = redisHelper.getSKey(appKey);
        }

        if (StringUtils.isEmpty(skey)) {
            RestResult result = new RestResult(ResultType.FAIL, "get no skey");
            FilterResponseRender.render(response, result);
            return;
        }

        // 计算MAC
        String payload;
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            // POST请求,将请求body作为payload进行加密计算
            payload = req.getReader()
                         .lines()
                         .collect(Collectors.joining(System.lineSeparator()));
        }
        else {
            // 其它请求,payload是请求path(带query string)
            payload = request.getRequestURI() +
                      (request.getQueryString() != null ? "?" + request.getQueryString() : "");
        }

        String calculatedMAC = MACHelper.calculateMAC(skey, payload);
        if (!mac.equals(calculatedMAC)) {
            RestResult result = new RestResult(ResultType.FAIL,
                    String.format("MAC not match. Expect: %s, actual: %s", mac, calculatedMAC));
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