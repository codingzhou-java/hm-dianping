package com.hmdp.utils;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class LoginInterceptor implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 判断是否需要拦截（ThreadLcoal中是否有用户）
        if (UserHolder.getUser() == null) {
            // 没有用户，需要拦截，返回401
            response.setStatus(401);
            return false;
        }
        // 有用户则放行
        return true;
    }
}