package com.itheima.reggie.common;


/**
 * 由于是工具类，所有方法都要设置成静态
 * 基于ThreadLocal封装的工具类，用于保存和获取当前登录用户的id，在LoginCheckFilter.doFilter中获取id
 */
public class BaseContext {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }
    public static Long getCurrentId() {
        return threadLocal.get();
    }
}
