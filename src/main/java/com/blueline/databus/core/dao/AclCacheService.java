package com.blueline.databus.core.dao;

import com.blueline.databus.core.datatype.AclInfo;
import com.blueline.databus.core.helper.TimeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * ACL缓存操作服务
 */
@Repository
public class AclCacheService {

    @Autowired
    private StringRedisTemplate redisTemplate4Acl;

    /**
     * 检测redis缓存中的acl信息,判断某个client(通过其appkey)能否访问api;
     * <p>
     *     acl cache的Key格式为 "GET /xxx/xxx" or "POST /yyy/yyy";
     *     acl cache的内容是个Map：键值对为{clientName}:{duration};
     * </p>
     *
     * @param api api地址(是缓存键值)
     * @param method 访问方法(HTTP方法,如GET,POST等)
     * @param appKey client的appkey
     * @return 结果状态
     * <ul>
     *     <li>0 - 缓存中没有该api对应值</li>
     *     <li>1 - 缓存中有记录,而且可以访问</li>
     *     <li><strong>-1</strong> - 缓存中有记录,但是记录显示无权限访问</li>
     *     <li><strong>-2</strong> - 缓存检查过程出现错误</li>
     * </ul>
     */
    public int checkAccess(String api, String method, String appKey) {
        try {
            // cache key is in format like "GET /xxx/xxx", "POST /yyy/yyy", etc.
            String cacheKey = String.format("%s %s", method.toUpperCase(), api);

            String duration = this.redisTemplate4Acl.opsForHash().get(cacheKey, appKey).toString();
            if (StringUtils.isEmpty(duration)) {
                return 0; // find no entry for this user/api
            }

            String nowDate = new SimpleDateFormat("HHmm").format(new Date());
            if (TimeHelper.isInDuration(nowDate, duration)) {
                return 1;
            }
            else {
                return -1;
            }
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
        this.redisTemplate4Acl.keys("*").forEach(cacheKey ->
            this.redisTemplate4Acl.opsForHash().keys(cacheKey).forEach(
                    appKey -> {
                        String[] key_pair = cacheKey.split(" ");
                        String method = key_pair[0];
                        String api = key_pair[1];
                        String duration = this.redisTemplate4Acl.opsForHash().get(cacheKey, appKey).toString();
                        result.add(new AclInfo(api, method, appKey.toString(), duration));
                    }
            )
        );
        return result;
    }

    /**
     * 将acl信息列表加载到缓存中
     * @param aclList acl信息列表
     * @param cleanUnknown 是否清除缓存中,不在参数aclList中的其它缓存acl信息
     * @return 设置的数目
     */
    private int loadAcl(List<AclInfo> aclList, boolean cleanUnknown) {
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
     * loadAcl(list, boolean)的重载,第二个参数使用false作为默认值，不清除设置范围外的值；
     *
     * <p>
     *     何时load acl记录到缓存有两种策略：
     * </p>
     * <ul>
     *     <li>系统已启动即调用此方法</li>
     *     <li>用户首次访问某条api时，如果数据库中存在acl记录，就load到缓存</li>
     * </ul>
     * <p>
     *     目前使用<strong>第二条</strong>策略
     * </p>
     *
     * @param aclList 要设置的acl信息列表
     * @return 加载的数目
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
        this.redisTemplate4Acl.opsForHash().put(cacheKey, aclInfo.getAppKey(), aclInfo.getDuration());
    }

    /**
     * 清除缓存中一条acl记录
     * @param cacheKey cache key
     * @param appKey 客户端app key
     */
    public void removeOneAcl(String cacheKey, String appKey) {
        this.redisTemplate4Acl.opsForHash().delete(cacheKey, appKey);
    }

    /**
     * 清除本缓存中的所有数据
     */
    public void flushDB() {
        try {
            this.redisTemplate4Acl.getConnectionFactory().getConnection().flushDb();
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}
