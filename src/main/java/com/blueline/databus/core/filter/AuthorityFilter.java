package com.blueline.databus.core.filter;

import com.blueline.databus.core.bean.RestResult;
import com.blueline.databus.core.config.VendorApiConfig;
import com.blueline.databus.core.bean.FilterResponseRender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

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
    private VendorApiConfig vendorApiConfig;

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest)req;

        String apiUrl = String.format(
                "%s?a=%s&n=%s&m=%s",
                vendorApiConfig.getAccessCheck(),
                request.getServletPath(),
                request.getHeader("x-appkey"), // 注意: api应该改为使用appkey
                request.getMethod());

        RestResult callResult = new RestTemplate().getForObject(apiUrl, RestResult.class);

        if (callResult.getType() == OK) {
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
