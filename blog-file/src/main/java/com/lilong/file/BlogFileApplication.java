package com.lilong.file;

import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.spring.EnableFileStorage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@Slf4j
@EnableCaching
@EnableFileStorage
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients("com.lilong.blogrpc")
@ComponentScan(basePackages = "com.lilong.*")
public class BlogFileApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogFileApplication.class, args);
        log.info("文件系统启动成功===========>success");
    }

}
