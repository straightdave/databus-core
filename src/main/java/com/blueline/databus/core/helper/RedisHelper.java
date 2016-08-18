package com.blueline.databus.core.helper;

import com.blueline.databus.core.datatype.AclInfo;
import com.blueline.databus.core.configtype.AdminConfig;
import com.blueline.databus.core.configtype.RedisConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Component
public class RedisHelper {
    private final Logger logger = Logger.getLogger(RedisHelper.class);

    private static JedisPool jedisPool = null;
    private RedisConfig redisConfig;
    private AdminConfig adminConfig;

    @Autowired
    private RedisHelper(RedisConfig redisConfig, AdminConfig adminConfig) {
        this.redisConfig = redisConfig;
        this.adminConfig = adminConfig;

        if (jedisPool == null) {
            jedisPool = new JedisPool(
                    new JedisPoolConfig(),
                    redisConfig.getHost(),
                    redisConfig.getPort());
        }
    }

    /**
     * 为测试目的,清除所有值
     * 仅在package内(测试也在同一个package)使用
     */
    void flushAll() {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.flushAll();
            logger.info("RedisHelper: flushAll was called");
        }
        catch (JedisException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * 清除一个db的数据,rollback时使用
     * 仅用于本类
     * @param db db的index
     */
    private void flushDB(int db) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(db);
            jedis.flushDB();
            logger.info("RedisHelper: flushDB: " + db);
        }
        catch (JedisException ex) {
            logger.error(ex.getMessage());
        }
    }

    public void recordAPICall(String apiPath) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(redisConfig.getDb_recordApi());
            Long result = jedis.incr(apiPath);
            logger.debug("RedisHelper: recordAPICall: " + apiPath + ", to: " + result);
        }
        catch (JedisException ex) {
            logger.error(ex.getMessage());
        }
    }

    public String getAPICallCount(String apiPath) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(redisConfig.getDb_recordApi());
            return jedis.get(apiPath);
        }
        catch (JedisException jex) {
            System.err.print("RedisHelperError: " + jex.getMessage());
        }
        return null;
    }

    /**
     * 检测redis中得acl信息,判断某client(通过其appkey)能否访问api
     * @param api api地址(键值)
     * @param method 访问方法(HTTP)
     * @param appkey client的appkey
     * @return true/false
     */
    public boolean checkAccess(String api, String method, String appkey) {
        boolean result = false;

        if (!StringUtils.isEmpty(appkey) && appkey.equals(adminConfig.getAppkey())) {
            // admin拥有所有api的权限
            return true;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(redisConfig.getDb_accessCache());
            String content = jedis.get(api);

            if (!StringUtils.isEmpty(content)) {
                ObjectMapper objectMapper = new ObjectMapper();
                AclInfo acl = objectMapper.readValue(content, AclInfo.class);

                String nowDate = new SimpleDateFormat("HHmm").format(new Date());
                String duration = acl.getDuration();

                if (method.equals(acl.getMethod())
                    && appkey.equals(acl.getAppkey())
                    && TimeHelper.isInDuration(nowDate, duration))
                {
                    result = true;
                }
                else {
                    logger.info(String.format("checkAccess Failed: %s", acl.toString()));
                }
            }
        }
        catch (JedisException | IOException ex) {
            logger.error("RedisHelperError: Check Access: " + ex.getMessage());
            return false;
        }
        return result;
    }

    /**
     * 将acl信息列表加载到redis中,会覆盖已有值
     * @param aclList acl列表
     * @param cleanUnknown 是否清除acl数据库中不在参数列表中的其他数据
     * @return <int>设置的数目</int>
     */
    int loadAcl(List<AclInfo> aclList, boolean cleanUnknown) {
        int count = 0;
        System.out.println("load acl...");

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(redisConfig.getDb_accessCache());
            for(AclInfo item : aclList) {
                jedis.set(item.getApi(), item.toString());
                count++;
            }
        }
        catch (JedisException ex) {
            logger.error("RedisHelper: loadAcl: " + ex.getMessage());
            flushDB(redisConfig.getDb_accessCache());
            count = -1;

        }
        return count;
    }

    /**
     * loadAcl(list, boolean)的重载,默认保留设置范围外的值
     * @param aclList 要设置的acl数据列表
     * @return <int>设置的数目</int>
     */
    int loadAcl(List<AclInfo> aclList) {
        return loadAcl(aclList, false);
    }
}