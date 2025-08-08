package com.itheima.reggie.filter;

import com.alibaba.fastjson2.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;

/*
检查用户是否登录的过滤器
 */
@Slf4j
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        log.info("拦截到请求：{}",request.getRequestURI());
        //不需要过滤器处理的请求路径
        String [] urls = new String[]{
            "/employee/login",
            "/employee/loginout",
            "/backend/**",
            "/front/**" ,
            "/favicon.ico",
            "/user/code", //移动端发送短信
            "/user/login" //移动端登录
        };
        boolean check = check(urls, request.getRequestURI());
        //请求路径不需要过滤器处理，直接放行
        if(!check){
            log.info("本次请求不需要处理{}",request.getRequestURI());
            filterChain.doFilter(request,response);
            return;
        }
        //已经登录，直接放行
        if((request.getSession().getAttribute("employee") != null)){
            log.info("用户已登录，用户ID为：{}",request.getSession().getAttribute("employee"));

            long empId = Long.parseLong(request.getSession().getAttribute("employee").toString());
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request,response);
            return;
        }
        //移动端用户已登录，直接放行
        if((request.getSession().getAttribute("user") != null)){
            log.info("用户已登录，用户ID为：{}",request.getSession().getAttribute("user"));

            long userId = Long.parseLong(request.getSession().getAttribute("user").toString());
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request,response);
            return;
        }

        log.info("用户未登录");
        //如果未登录则返回未登录结果，通过输出流方式向客户端页面响应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
    }

    /**
     * 路径匹配，检查本次请求是否需要过滤器处理
     * @param urls 无需处理的请求路径
     * @param requestURI 此次请求
     * @return boolean
     */
    public boolean check(String [] urls,String requestURI) {
        for(String path : urls){
            boolean match=PATH_MATCHER.match(path,requestURI);
            if(match)
                return false;
        }
        return true;
    }
}
