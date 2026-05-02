package com.hmdp.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.UserDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RefreshTokenInterceptor implements HandlerInterceptor {

    private StringRedisTemplate stringRedisTemplate;

    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1. 获取请求头中的token
        String token = request.getHeader("authorization");
        if(StrUtil.isBlank(token)){
            //2. 不存在，说明用户未登录，直接放行，让LoginInterceptor来判断是否需要拦截
            return true;
        }
        //2. 基于token获取redis中用户
        String key = RedisConstants.LOGIN_USER_KEY + token;
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(key);
        //3. 判断用户是否存在
        if (userMap.isEmpty()) {
            //4. 不存在，拦截
            response.setStatus(401);
            return false;
        }
        //5. 将查询到的hash数据转为UserDTO对象
        UserDTO user = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
        //存在，保存
        UserHolder.saveUser(user);
        //刷新时间
        stringRedisTemplate.expire(key, RedisConstants.LOGIN_USER_TTL, TimeUnit.SECONDS);
        //6. 放行
        return true;
    }

/**
 * 在请求处理完成后执行的方法，通常用于清理资源
 * @param request 当前HTTP请求对象
 * @param response 当前HTTP响应对象
 * @param handler 请求处理的方法处理器
 * @param ex 处理过程中发生的异常，如果没有异常则为null
 * @throws Exception 可能抛出的异常
 */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    // 移除当前线程中保存的用户信息，防止内存泄漏
        UserHolder.removeUser();
    }
}
