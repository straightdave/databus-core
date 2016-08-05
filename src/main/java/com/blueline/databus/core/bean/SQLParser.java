package com.blueline.databus.core.bean;

import java.util.Map;

import com.blueline.databus.core.config.DefaultConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class SQLParser {

    private DefaultConfig defaultConfig;

    @Autowired
    private SQLParser(DefaultConfig config) {
        this.defaultConfig = config;
    }

    /**
     * 分析Query String,将其转化为Select-SQL语句的条件clause
     * @param paramMap request获取的parameter Map
     * @return SQL查询clause
     */
	public String parseQueryString4Select(Map<String, String[]> paramMap) {
	    // 预处理map,确保包含meta参数
        if (!paramMap.containsKey("_by")) {
            // 默认排序字段是id,这要求库中表都应该含有id字段
            paramMap.put("_by", new String[] { "id" });
        }
        if (!paramMap.containsKey("_order")) {
            paramMap.put("_order", new String[] { "asc" });
        }
        if (!paramMap.containsKey("_skip")) {
            paramMap.put("_skip", new String[] { "0" });
        }
        if (!paramMap.containsKey("_take")) {
            paramMap.put("_take", new String[] {
                StringUtils.isEmpty(defaultConfig.getDefaultTakes()) ? "10" : defaultConfig.getDefaultTakes()
            });
        }

        StringBuilder sqlQuery = new StringBuilder("WHERE ");

        // 先拼接非meta参数
        paramMap.entrySet()
                .stream()
                .filter(entry -> !entry.getKey().startsWith("_"))
                .forEach(entry -> {
                    String k = entry.getKey();
                    String[] v = entry.getValue();

                    // 对所有 *_start或*_stop 参数,如若同名多值,只支持其第一个值,即取v[0]
                    if (k.endsWith("_start")) {
                        String columnName = k.substring(0, k.length() - "_start".length());
                        sqlQuery.append(String.format("`%s`>='%s' AND ", columnName, v[0]));
                    }
                    else if (k.endsWith("_stop")) {
                        String columnName = k.substring(0, k.length() - "_stop".length());
                        sqlQuery.append(String.format("`%s`<='%s' AND ", columnName, v[0]));
                    }

                    // 对于其他普通参数值对,如遇同名多值则重复拼接
                    else {
                        for (String value : v) {
                            sqlQuery.append(String.format("`%s`='%s' AND ", k, value));
                        }
                    }
                });

        // 在条件clause最后加上1=1,有助于去掉上面拼接时遗留的尾部的AND的功能
        // 并且,在没有条件clause的时候,1=1也可以去掉WHERE的功能
        sqlQuery.append("1=1 ");

        // 最后拼接meta参数
        sqlQuery.append(String.format("ORDER BY `%s` %s ",
                paramMap.get("_by")[0], paramMap.get("_order")[0].toUpperCase()));
        sqlQuery.append(String.format("LIMIT %s,%s",
                paramMap.get("_skip")[0], paramMap.get("_take")[0]));

        return sqlQuery.toString();
	}

    /**
     * 分析Query String,将其转化为Delete-SQL语句的条件clause
     * @param paramMap 从request调用getParamsterMap获取的map
     * @return sql clauses
     */
    public String parseQueryString4Delete(Map<String, String[]> paramMap) {
        return "";
    }

    /**
     *
     * @param JsonBody
     * @return
     */
    public String parseQueryString4CreateTable(String JsonBody) {

        return "";
    }
}
