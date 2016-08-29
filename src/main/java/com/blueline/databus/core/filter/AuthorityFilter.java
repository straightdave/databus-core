package com.blueline.databus.core.filter;

import com.blueline.databus.core.dao.AclCacheService;
import com.blueline.databus.core.dao.SysDBDao;
import com.blueline.databus.core.datatype.AclInfo;
import com.blueline.databus.core.datatype.ClientInfo;
import com.blueline.databus.core.datatype.RestResult;
import com.blueline.databus.core.helper.FilterResponseRender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

import static com.blueline.databus.core.datatype.ResultType.*;

/**
 * 这个filter次一级
 * 验证请求者是否有权限请求api代表的资源
 */
public class AuthorityFilter implements Filter {

    @Value("${admin.appkey}")
    private String adminAppKey;

    @Autowired
    private AclCacheService aclCacheService;

    @Autowired
    private SysDBDao sysDBDao;

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest)req;
        String api    = request.getRequestURI();
        String method = request.getMethod();
        String appKey = request.getHeader("x-appkey");

        try {
            if (appKey.equalsIgnoreCase(adminAppKey)) {
                System.out.println("==> admin pass");
                chain.doFilter(req, resp);
                return;
            }

            ClientInfo client = sysDBDao.getClientByAppKey(appKey);

            int redisCheckState = aclCacheService.checkAccess(api, method, client.getName());

            if (redisCheckState == 1) {
                System.out.println("==> check pass");
                chain.doFilter(req, resp);
                return;
            }
            else if (redisCheckState == 0 || redisCheckState == -2) {
                System.out.println("==> Cache return:" + redisCheckState + "; will check DB...");

                List<AclInfo> aclInfoList = sysDBDao.checkAclInfo(api, method, client.getName());
                if (aclInfoList != null && aclInfoList.size() > 0) {
                    System.out.println("==> check pass, load to cache...");
                    aclCacheService.loadOneAcl(aclInfoList.get(0)); // by default load the first of result set
                    System.out.println("==> load done");
                    chain.doFilter(req, resp);
                    return;
                }
            }

            System.out.println("==> no access");
            RestResult result = new RestResult(FAIL, "No Access");
            FilterResponseRender.render(resp, result);
        }
        catch (Exception ex) {
            System.out.println("==> no access due to error: " + ex.getMessage());
            RestResult result = new RestResult(ERROR, "No Access: " + ex.getMessage());
            FilterResponseRender.render(resp, result);
        }
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {}

    @Override
    public void destroy() {}
}
