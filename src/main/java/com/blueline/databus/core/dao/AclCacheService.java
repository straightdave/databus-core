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

@Repository
public class AclCacheService {

    @Value("${admin.appkey}")
    private String adminAppKey;

    @Value("${admin.skey}")
    private String adminSKey;


    @Autowired
    private StringRedisTemplate redisTemplate4Acl;


    /**
     * 检测redis中得acl信息,判断某client(通过其appkey)能否访问api
     * @param api api地址(键值)
     * @param method 访问方法(HTTP)
     * @param appkey client的appkey
     * @return status
     */
    public int checkAccess(String api, String method, String appkey)
            throws InternalException, IOException {

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
            TimeHelper.isInDuration(nowDate, duration)) {
            return 1; // can access by checking cache
        }
        else {
            return -1; // cannot access by checking cache
        }
    }

    /**
     * 将acl信息列表加载到redis中,会覆盖已有值
     * @param aclList acl列表
     * @param cleanUnknown 是否清除acl数据库中不在参数列表中的其他数据
     * @return <int>设置的数目</int>
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
     * loadAcl(list, boolean)的重载,默认保留设置范围外的值
     * @param aclList 要设置的acl数据列表
     * @return <int>设置的数目</int>
     */
    public int loadAcl(List<AclInfo> aclList) {
        return loadAcl(aclList, false);
    }

    public void loadOneAcl(AclInfo aclInfo) {
        this.redisTemplate4Acl.opsForValue().set(aclInfo.getApi(), aclInfo.toString());
    }

    /**
     * 清除本DB中所有数据
     * 主要用作测试目的
     */
    public void flushDB() {
        this.redisTemplate4Acl.getConnectionFactory().getConnection().flushDb();
    }
}
