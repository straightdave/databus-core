package com.blueline.databus.core.filter;

import com.blueline.databus.core.datatype.RestResult;
import com.blueline.databus.core.helper.FilterResponseRender;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.*;
import java.io.IOException;

import static com.blueline.databus.core.datatype.ResultType.FAIL;

public class AdminFilter implements Filter {

    @Value("${admin.appkey}")
    private String adminAppKey;

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)req;
        String appKey = request.getHeader("x-appkey");

        if (appKey.equalsIgnoreCase(adminAppKey)) {
            chain.doFilter(req, resp);
        }
        else {
            RestResult result = new RestResult(FAIL, "admin only");
            FilterResponseRender.render(resp, result);
        }
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {}

    @Override
    public void destroy() {}
}