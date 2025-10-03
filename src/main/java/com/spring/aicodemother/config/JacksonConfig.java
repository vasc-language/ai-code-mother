package com.spring.aicodemother.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Jackson配置类
 * 解决前端JavaScript数字精度丢失问题
 * 将Long类型序列化为字符串
 *
 * @author system
 */
@Configuration
public class JacksonConfig {

    /**
     * 自定义ObjectMapper，将Long类型序列化为字符串
     * 避免JavaScript数字精度丢失问题
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        
        // 创建自定义模块
        SimpleModule module = new SimpleModule();
        // 将Long类型序列化为字符串
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);
        module.addSerializer(Long.class, ToStringSerializer.instance);
        
        // 注册模块
        objectMapper.registerModule(module);
        
        return objectMapper;
    }
}
