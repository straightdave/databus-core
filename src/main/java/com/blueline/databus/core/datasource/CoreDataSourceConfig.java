package com.blueline.databus.core.datasource;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;

@Configuration
public class CoreDataSourceConfig {

    @Value("${db.core.driverManager}")
    private String coreDriverManager;

    @Value("${db.core.url}")
    private String coreUrl;

    @Value("${db.core.username}")
    private String coreUsername;

    @Value("${db.core.password}")
    private String corePassword;

    @Bean
    public DataSource dsCore() throws PropertyVetoException {
        ComboPooledDataSource coreDS = new ComboPooledDataSource();
        coreDS.setDriverClass(coreDriverManager);
        coreDS.setJdbcUrl(coreUrl);
        coreDS.setUser(coreUsername);
        coreDS.setPassword(corePassword);
        return coreDS;
    }

    @Bean
    public JdbcTemplate templateCore(DataSource dsCore) throws PropertyVetoException {
        return new JdbcTemplate(dsCore);
    }
}
