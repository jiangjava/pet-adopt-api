package com.kmbeast.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

@Aspect
@Component
public class IdempotentAspect {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String LUA_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "    return redis.call('del', KEYS[1]) " +
                    "else " +
                    "    return 0 " +
                    "end";

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }
        HttpServletRequest request = attributes.getRequest();

        // 获取令牌
        String token = null;
        if("header".equalsIgnoreCase(idempotent.location())) {
            token = request.getHeader(idempotent.location());
        } else if ("parameter".equalsIgnoreCase(idempotent.location())) {
            token = request.getParameter(idempotent.key());}

        if (token == null || token.isEmpty()) {
            throw new RuntimeException("幂等性令牌不能为空");
        }

        String redisKey = "idempotent:token:" + token;
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(LUA_SCRIPT, Long.class);
        Long result = redisTemplate.execute(redisScript, Collections.singletonList(redisKey), token);

        if (result == null || result == 0) {
            throw new RuntimeException("请勿重复提交");
        }

        return joinPoint.proceed();
    }
 }
