package com.blueline.databus.core.dao;

import com.blueline.databus.core.datatype.AclInfo;
import com.blueline.databus.core.exception.InternalException;
import com.blueline.databus.core.helper.TimeHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * ACL缓存操作服务
 */
@Repository
public class AclCacheService {

    @Value("${admin.appkey}")
    private String adminAppKey;

    @Value("${admin.skey}")
    private String adminSKey;

    @Autowired
    private StringRedisTemplate redisTemplate4Acl;

    /**
     * 检测redis缓存中的acl信息,判断某个client(通过其appkey)能否访问api
     * @param api api地址(是缓存键值)
     * @param method 访问方法(HTTP方法,如GET,POST等)
     * @param appkey client的appkey
     * @return 结果状态
     * <ul>
     *     <li>0 - 缓存中没有该api对应值</li>
     *     <li>1 - 缓存中有记录,而且可以访问</li>
     *     <li>2 - 表示输入的appkey表明访问者是管理员,可以访问</li>
     *     <li><strong>-1</strong> - 缓存中有记录,但是记录显示无权限访问</li>
     *     <li><strong>-2</strong> - 缓存检查过程出现错误</li>
     * </ul>
     */
    public int checkAccess(String api, String method, String appkey) {
        try {
            if (!StringUtils.isEmpty(appkey) && appkey.equals(this.adminAppKey)) {
                return 2; // this is admin, ignore checking
            }

            String content = this.redisTemplate4Acl.opsForValue().get(api);

            if (StringUtils.isEmpty(content)) {
                return 0; // no access record in redis
            }

            AclInfo acl = new ObjectMapper().readValue(content, AclInfo.class);

            String nowDate = new SimpleDateFormat("HHmm").format(new Date());
            String duration = acl.getDuration();

            if (method.equals(acl.getMethod()) &&
                appkey.equals(acl.getAppkey()) &&
                TimeHelper.isInDuration(nowDate, duration)
            ) {
                return 1; // can access by checking cache
            }
            else {
                return -1; // cannot access by checking cache
            }
        }
        catch (Exception ex) {
            // eat all exceptions
            return -2; // exit by error
        }
    }

    /**
     * 将acl信息列表加载到缓存中
     * @param aclList acl信息列表
     * @param cleanUnknown 是否清除缓存中,不在参数acl信息列表中的其它缓存acl信息
     * @return 设置的数目
     */
    public int loadAcl(List<AclInfo> aclList, boolean cleanUnknown) {
        int count = 0;

        if (cleanUnknown) {
            this.redisTemplate4Acl.getConnectionFactory().getConnection().flushDb();
        }

        for(AclInfo item : aclList) {
            this.redisTemplate4Acl.opsForValue().set(item.getApi(), item.toString());
            count++;
        }
        return count;
    }

    /**
     * loadAcl(list, boolean)的重载,默认不清除设置范围外的值
     * @param aclList 要设置的acl信息列表
     * @return 设置的数目
     */
    public int loadAcl(List<AclInfo> aclList) {
        return loadAcl(aclList, false);
    }

    /**
     * 载入一条acl信息,不干扰缓存中其它数据
     * @param aclInfo acl信息
     */
    public void loadOneAcl(AclInfo aclInfo) {
        this.redisTemplate4Acl.opsForValue().set(aclInfo.getApi(), aclInfo.toString());
    }

    /**
     * 清除本缓存中的所有数据
     * 主要用于测试目的
     */
    public void flushDB() {
        this.redisTemplate4Acl.getConnectionFactory().getConnection().flushDb();
    }
}
