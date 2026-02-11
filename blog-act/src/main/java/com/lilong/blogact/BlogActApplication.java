package com.lilong.blogact;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@Slf4j
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients("com.lilong.blogrpc")
@ComponentScan(basePackages = "com.lilong.*")
public class BlogActApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogActApplication.class, args);
        log.info("博客行为系统启动成功======>success");
    }

}
