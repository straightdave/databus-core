package com.blueline.databus.core.filter;

import com.blueline.databus.core.datatype.RestResult;
import com.blueline.databus.core.helper.FilterResponseRender;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.blueline.databus.core.datatype.ResultType.ERROR;

/**
 * 添加CORS头部
 */
public class CorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) resp;
        response.setHeader("Access-Control-Allow-Methods", "GET,POST,DELETE,PUT,HEAD");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Max-Age", "3600");
        chain.doFilter(req, resp);
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {}

    @Override
    public void destroy() {}
}
