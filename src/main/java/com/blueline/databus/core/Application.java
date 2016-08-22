package com.blueline.databus.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
@EnableAutoConfiguration(exclude={
    DataSourceAutoConfiguration.class,
    RedisRepositoriesAutoConfiguration.class,
    RedisAutoConfiguration.class})
public class Application  {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}