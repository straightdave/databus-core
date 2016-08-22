package com.blueline.databus.core.datasource;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DriverManagerDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;

@Configuration
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
}
