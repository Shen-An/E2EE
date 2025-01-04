package com.easyChat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan(basePackages = "com.easyChat.mappers")
@EnableTransactionManagement
@EnableScheduling
@EnableAsync
public class easyChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(easyChatApplication.class, args);
    }
}
