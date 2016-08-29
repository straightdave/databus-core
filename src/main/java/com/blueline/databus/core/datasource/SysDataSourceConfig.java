package com.blueline.databus.core.datasource;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DriverManagerDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;

/**
 * 使用JDBC(c3p0连接池)
 * 配置用于系统数据库(数据总线的系统数据定义)的数据源
 */
@Configuration
@EnableTransactionManagement
public class SysDataSourceConfig {

    @Value("${db.sys.driverManager}")
    private String sysDriverManager;

    @Value("${db.sys.url}")
    private String sysUrl;

    @Value("${db.sys.username}")
    private String sysUsername;

    @Value("${db.sys.password}")
    private String sysPassword;

    @Bean
    public DataSource dsSys() throws PropertyVetoException {
        ComboPooledDataSource sysDS = new ComboPooledDataSource();
        sysDS.setDriverClass(sysDriverManager);
        sysDS.setJdbcUrl(sysUrl);
        sysDS.setUser(sysUsername);
        sysDS.setPassword(sysPassword);
        return sysDS;
    }

    @Bean
    public JdbcTemplate templateSys(DataSource dsSys) throws PropertyVetoException {
        return new JdbcTemplate(dsSys);
    }

    @Bean
    public PlatformTransactionManager txManager() throws PropertyVetoException {
        return new DataSourceTransactionManager(dsSys());
    }
}
