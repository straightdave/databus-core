package com.blueline.databus.core;

import com.blueline.databus.core.filter.AuthenticationFilter;
import com.blueline.databus.core.filter.AuthorityFilter;
import com.blueline.databus.core.filter.CorsFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppFilterConfiguration {

    @Bean
    public FilterRegistrationBean authenticationFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(authenticationFilter());
        registration.addUrlPatterns("/api/*");
        registration.setName("authenticationFilter");
        registration.setOrder(2);
        return registration;
    }

    @Bean
    public AuthenticationFilter authenticationFilter() {
        return new AuthenticationFilter();
    }

    @Bean
    public FilterRegistrationBean authorityFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(authorityFilter());
        registration.addUrlPatterns("/api/*");
        registration.setName("authorityFilter");
        registration.setOrder(3);
        return registration;
    }

    @Bean
    public AuthorityFilter authorityFilter() {
        return new AuthorityFilter();
    }

    @Bean
    public FilterRegistrationBean corsFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(corsFilter());
        registration.addUrlPatterns("/*");
        registration.setName("corsFilter");
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public CorsFilter corsFilter() {
        return new CorsFilter();
    }
}
