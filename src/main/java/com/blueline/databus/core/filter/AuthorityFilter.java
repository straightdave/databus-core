package com.blueline.databus.core.filter;

import com.blueline.databus.core.bean.RestResult;
import com.blueline.databus.core.helper.FilterResponseRender;
import com.blueline.databus.core.helper.RedisHelper;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static com.blueline.databus.core.bean.ResultType.*;

/**
 * 这个filter次一级
 * 验证请求者是否有权限请求api代表的资源
 */
public class AuthorityFilter implements Filter {

    @Autowired
    private RedisHelper redisHelper;

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest)req;

        if (redisHelper.checkAccess(
                request.getRequestURI(),
                request.getMethod(),
                request.getHeader("x-appkey")))
        {
            chain.doFilter(req, resp);
        }
        else {
            RestResult result = new RestResult(FAIL, "No Access");
            FilterResponseRender.render(resp, result);
        }
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {}

    @Override
    public void destroy() {}
}
