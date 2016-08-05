package com.blueline.databus.core;

import com.blueline.databus.core.filter.AuthenticationFilter;
import com.blueline.databus.core.filter.AuthorityFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application  {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public FilterRegistrationBean authenticationFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(authenticationFilter());
        registration.addUrlPatterns("/api/*");
        registration.setName("authenticationFilter");
        registration.setOrder(1);
        return registration;
    }

    @Bean(name="authenticationFilter")
    public AuthenticationFilter authenticationFilter() {
        return new AuthenticationFilter();
    }

    @Bean
    public FilterRegistrationBean authorityFilterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(authorityFilter());
        registration.addUrlPatterns("/api/*");
        registration.setName("authorityFilter");
        registration.setOrder(2);
        return registration;
    }

    @Bean(name="authorityFilter")
    public AuthorityFilter authorityFilter() {
        return new AuthorityFilter();
    }

}