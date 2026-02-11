package com.lilong.blogaigc;

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
public class BlogAigcApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogAigcApplication.class, args);
        log.info("博客AI智能体管理系统启动成功===========>success");
    }

}
