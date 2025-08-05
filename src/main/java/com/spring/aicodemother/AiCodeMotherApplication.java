package com.spring.aicodemother;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用程序启动类
 * 
 * @author system
 */
@SpringBootApplication
@MapperScan("com.spring.aicodemother.mapper") // 告诉Spring Boot自动扫描并注册指定包路径下的MyBatis Mapper接口
public class AiCodeMotherApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiCodeMotherApplication.class, args);
    }

}
