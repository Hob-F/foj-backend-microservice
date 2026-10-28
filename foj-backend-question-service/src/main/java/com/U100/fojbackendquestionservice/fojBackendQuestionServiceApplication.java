package com.U100.fojbackendquestionservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.U100.fojbackendquestionservice.mapper")
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@ComponentScan("com.U100")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.U100.fojbackendserviceclient.service"})
public class fojBackendQuestionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(fojBackendQuestionServiceApplication.class, args);
    }

}
