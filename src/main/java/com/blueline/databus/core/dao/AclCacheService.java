package com.blueline.databus.core.dao;

import com.blueline.databus.core.datatype.AclInfo;
import com.blueline.databus.core.helper.TimeHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * ACL缓存操作服务
 */
@Repository
public class AclCacheService {
    private ObjectMapper om = new ObjectMapper();

    @Autowired
    private StringRedisTemplate redisTemplate4Acl;

    /**
     * 检测redis缓存中的acl信息,判断某个client(通过其name)能否访问api
     * cache Key in format like "GET /xxx/xxx" or "POST /yyy/yyy"
     * cache content is hash with {clientName}:{Acl json} as k-v pair
     * @param api api地址(是缓存键值)
     * @param method 访问方法(HTTP方法,如GET,POST等)
     * @param clientName client的name
     * @return 结果状态
     * <ul>
     *     <li>0 - 缓存中没有该api对应值</li>
     *     <li>1 - 缓存中有记录,而且可以访问</li>
     *     <li><strong>-1</strong> - 缓存中有记录,但是记录显示无权限访问</li>
     *     <li><strong>-2</strong> - 缓存检查过程出现错误</li>
     * </ul>
     */
    public int checkAccess(String api, String method, String clientName) {
        try {
            // cache key is in format like "GET /xxx/xxx", "POST /yyy/yyy", etc.
            String cacheKey = String.format("%s %s", method.toUpperCase(), api);

            AclInfo aclInfo = null;
            if (this.redisTemplate4Acl.opsForHash().hasKey(cacheKey, clientName)) {
                Object obj = this.redisTemplate4Acl.opsForHash().get(cacheKey, clientName);
                aclInfo = om.readValue(obj.toString(), AclInfo.class);
            }

            if (aclInfo != null) {
                String nowDate = new SimpleDateFormat("HHmm").format(new Date());
                String duration = aclInfo.getDuration();

                if (method.equalsIgnoreCase(aclInfo.getMethod()) &&
                    clientName.equals(aclInfo.getClientName())   &&
                    TimeHelper.isInDuration(nowDate, duration)
                ) {
                    return 1; // can access by checking cache
                }
                else {
                    return -1; // cannot access by checking cache
                }
            }

            return 0; // has no info in cache
        }
        catch (Exception ex) {
            // 不抛出异常是因为cache失效,系统默认会继续通过查询数据库处理
            return -2;
        }
    }

    /**
     * 返回目前缓存中所有acl信息
     * @return AclInfo实例列表
     */
    public List<AclInfo> dumpAllAcl() {
        List<AclInfo> result = new LinkedList<>();
        this.redisTemplate4Acl.keys("\\w{1,10}\\s/.*").forEach(key ->
            this.redisTemplate4Acl.opsForHash().values(key).forEach(acl -> {
                try {
                    om.readValue(acl.toString(), AclInfo.class);
                } catch (IOException ex) { /* eat it */ }
            })
        );
        return result;
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
            flushDB();
        }

        for(AclInfo item : aclList) {
            String cacheKey = String.format("%s %s", item.getMethod().toUpperCase(), item.getApi());
            this.redisTemplate4Acl.opsForValue().set(cacheKey, item.toString());
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
        String cacheKey = String.format("%s %s", aclInfo.getMethod().toUpperCase(), aclInfo.getApi());
        this.redisTemplate4Acl.opsForValue().set(cacheKey, aclInfo.toString());
    }

    /**
     * 清除缓存中一条acl信息
     * @param aclInfo 要清除的acl信息(AclInfo实例)
     */
    public void removeOneAcl(AclInfo aclInfo) {
        String cacheKey = String.format("%s %s", aclInfo.getMethod().toUpperCase(), aclInfo.getApi());
        this.redisTemplate4Acl.delete(cacheKey);
    }

    /**
     * 清除本缓存中的所有数据
     * 主要用于测试目的
     */
    public void flushDB() {
        this.redisTemplate4Acl.getConnectionFactory().getConnection().flushDb();
    }
}
