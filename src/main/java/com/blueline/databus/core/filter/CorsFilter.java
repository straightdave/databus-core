package com.blueline.databus.core.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * filter定义:添加CORS支持
 * Spring官方doc等处记录的方法在Springboot中都不生效,暂时以filter形式实现
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
