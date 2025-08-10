package com.itheima.reggie.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itheima.reggie.common.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//由于IDEA默认只访问static和template这两个目录下的静态资源，所以需要有这个配置类
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    /**
     * 设置静态资源映射
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("正在进行静态资源映射...");
        registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");
        registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");
    }

    /**
     * 扩展mvc框架的消息转换器，基于/common/JacksonObjectMapper.java,用于
     * 解决JS无法准确处理19位的long型数据导致的精度问题
     * 业务代码 -> 使用提供的 @Primary ObjectMapper -> long 转 string。
     * springdoc 的代码 -> 使用它内部的 ObjectMapper -> 生成标准的 OpenAPI JSON。
     */
    @Bean
    @Primary
    public ObjectMapper jacksonObjectMapper() {
        return new JacksonObjectMapper();
    }
}
