package com.blueline.databus.core.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * filter定义:添加CORS支持,为HTTP response添加相应的HTTP头部;
 * Spring官方doc等处记录的方法,在SpringBoot中都不生效,暂时以filter形式实现
 *
 * 添加的头部:
 * <ul>
 *     <li><strong>Access-Control-Allow-Methods</strong>: GET,POST,DELETE,PUT,HEAD</li>
 *     <li><strong>Access-Control-Allow-Origin</strong>: *</li>
 *     <li><strong>Access-Control-Max-Age</strong>: 3600</li>
 * </ul>
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
