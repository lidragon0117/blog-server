package com.lilong.blogclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@Slf4j
@EnableDiscoveryClient
@EnableFeignClients("com.lilong.blogrpc")
@ComponentScan(basePackages = "com.lilong.*")
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class BlogClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogClientApplication.class, args);
        log.info("博客客户端启动成功======>success");
    }

}
