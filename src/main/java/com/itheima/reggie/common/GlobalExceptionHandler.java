package com.itheima.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;


/**
 * 全局异常处理，底层基于代理
 * 捕获RestController和Controller抛出的异常
 */
@Slf4j
@ResponseBody
@ControllerAdvice(annotations={RestController.class, Controller.class})
public class GlobalExceptionHandler {

    //异常处理方法
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex ) {
        log.error(ex.getMessage());

        if(ex.getMessage().contains("Duplicate entry")) {
            String [] split=ex.getMessage().split(" ");
            String key = split[2];
            return R.error(key + "已存在 ！"); //记得要return
        }

        return R.error("未知错误");
    }

    //处理自定义的业务异常
    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException ex ) {
        log.error(ex.getMessage());
        return R.error(ex.getMessage());
    }
}
