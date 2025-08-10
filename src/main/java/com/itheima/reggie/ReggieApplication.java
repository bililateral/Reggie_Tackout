package com.itheima.reggie;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@EnableCaching //开启缓存，菜品的缓存没有用SpringCache，而套餐的缓存则使用了SpringCache
@ServletComponentScan /*让过滤器检查后端接收的请求*/
@SpringBootApplication
@EnableTransactionManagement //开启数据库事务注解的支持功能
public class ReggieApplication {
//管理员用户名：admin  密码：123456
    public static void main(String[] args) {
        SpringApplication.run(ReggieApplication.class, args);
        log.info("系统启动成功...");
    }
}
