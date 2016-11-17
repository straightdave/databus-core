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
 * Authority filter通过检查acl记录,
 * 验证请求者是否有权限请求api代表的资源
 */
public class AuthorityFilter implements Filter {

    @Value("${admin.appkey}")
    private String adminAppKey;

    @Autowired
    private AclCacheService aclCacheService;

    @Autowired
    private SysDBDao sysDBDao;

    /**
     * 从HTTP请求中获取:
     * <ul>
     *     <li>请求地址</li>
     *     <li>请求方法</li>
     *     <li><strong>x-appkey</strong>HTTP请求头</li>
     * </ul>
     *
     * <p>
     * 如果appkey显示是admin,则放行,因为admin具有所有API(数据API和系统API)的权限;
     * 不用担心appkey被伪造冒充admin,因为在上一层的authentication filter中,已经验证了skey,
     * 到达这里的请求,如果x-appkey头部是admin的appkey,那么它能确定就是admin(只要admin的skey没有泄露)
     * </p>
     *
     * <p>
     * 如果acl记录缓存(redis)没有启动或异常,服务将从数据库中查询acl记录
     * </p>
     *
     * <p>
     * 如果缓存中没有acl记录,但是通过查询数据库发现,将加载这条acl记录到缓存
     * </p>
     *
     * @param req servlet请求
     * @param resp servlet返回
     * @param chain filter链
     * @throws IOException 检验acl记录缓存中json格式数据可能抛出IOException
     * @throws ServletException 可能抛出ServletException
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest)req;
        String api    = request.getRequestURI();
        String method = request.getMethod();
        String appKey = request.getHeader("x-appkey");

        try {
            // admin can access to any api
            // and no need to load its acl record (it doesn't have)
            if (appKey.equalsIgnoreCase(adminAppKey)) {
                chain.doFilter(req, resp);
                return;
            }

            // bypass APIs not in black list
            // now only DDL and DML APIs are protected
            if (!api.startsWith("/api/def") && !api.startsWith("/api/data")) {
                chain.doFilter(req, resp);
                return;
            }

            // first use this app key to check cache
            int redisCheckState = aclCacheService.checkAccess(api, method, appKey);
            if (redisCheckState == 1) {
                // acl exists in cache, good
                chain.doFilter(req, resp);
                return;
            }
            else if (redisCheckState == 0 || redisCheckState == -2) {
                // if acl doesn't exist in cache or error happened while search cache
                // search the acl in database
                List<AclInfo> aclInfoList = sysDBDao.checkAclInfoByAppKey(api, method, appKey);

                // if acl exists in database, good. then load it to cache
                if (aclInfoList != null && aclInfoList.size() > 0) {
                    // 理论上只有一个满足条件的acl被找到，这里如果找到多个，只取第一条load至缓存
                    aclCacheService.loadOneAcl(aclInfoList.get(0));
                    chain.doFilter(req, resp);
                    return;
                }
            }

            RestResult result = new RestResult(FAIL, "No Access");
            FilterResponseRender.render(resp, result);
        }
        catch (Exception ex) {
            RestResult result = new RestResult(ERROR, "No Access: " + ex.getMessage());
            FilterResponseRender.render(resp, result);
        }
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {}

    @Override
    public void destroy() {}
}
