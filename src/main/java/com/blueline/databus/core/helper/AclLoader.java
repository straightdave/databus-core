package com.blueline.databus.core.helper;

import com.blueline.databus.core.dao.SysDBDao;
import com.blueline.databus.core.datatype.AclInfo;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class AclLoader {
    private static final Logger logger = Logger.getLogger(AclLoader.class);

    private final SysDBDao sysDBDao;

    private final RedisHelper redisHelper;

    @Autowired
    private AclLoader(SysDBDao sysDBDao, RedisHelper redisHelper) {
        this.sysDBDao = sysDBDao;
        this.redisHelper = redisHelper;
    }

    public int preloadAcl() {
        try {
            List<AclInfo> aclList = sysDBDao.getAllAclInfo();
            return redisHelper.loadAcl(aclList, true); // 清理其它数据,重新加载全部
        }
        catch (Exception ex) {
            logger.error("PreloadAcl: " + ex.getMessage());
            ex.printStackTrace();
            return -1;
        }
    }

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
