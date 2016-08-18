package com.blueline.databus.core.datasource;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.beans.PropertyVetoException;

@Configuration
public class C3p0DataSourceConfig {

    @Value("${db.core.driverManager}")
    private String driverManager;

    @Value("${db.core.url}")
    private String url;

    @Value("${db.core.username}")
    private String username;

    @Value("${db.core.password}")
    private String password;

    @Bean
    public ComboPooledDataSource dataSource() throws PropertyVetoException {
        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        dataSource.setDriverClass(this.driverManager);
        dataSource.setJdbcUrl(this.url);
        dataSource.setUser(this.username);
        dataSource.setPassword(this.password);
        return dataSource;
    }
}
