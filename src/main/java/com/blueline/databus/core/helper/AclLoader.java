package com.blueline.databus.core.helper;

import com.blueline.databus.core.bean.AclInfo;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.List;

public class AclLoader {
    private static final Logger logger = Logger.getLogger(AclLoader.class);

    @Autowired
    private SysDBHelper sysDBHelper;

    @Autowired
    private RedisHelper redisHelper;

    private static AclLoader instance = null;

    private AclLoader() {}

    public static AclLoader getInstance() {
        if (instance == null) {
            return new AclLoader();
        }
        return instance;
    }

    @PostConstruct
    public int preloadAcl() {
        try {
            List<AclInfo> aclList = sysDBHelper.getAclInfo();
            return redisHelper.loadAcl(aclList, true); // 清理其它数据,重新加载全部
        }
        catch (Exception ex) {
            logger.error("PreloadAcl: " + ex.getMessage());
            return -1;
        }
    }

    @PostConstruct
    public int updateAcl(List<AclInfo> aclList) {
        try {
            return redisHelper.loadAcl(aclList);
        }
        catch (Exception ex) {
            logger.error("UpdateAcl: " + ex.getMessage());
            return -1;
        }
    }
}
