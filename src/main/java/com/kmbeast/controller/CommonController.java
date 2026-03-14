package com.kmbeast.controller;

import com.kmbeast.pojo.api.ApiResult;
import com.kmbeast.pojo.api.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1.0/pet-adopt-api/common")
public class CommonController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @GetMapping("/idempotent-token")
    public Result<String> getIdempotentToken() {
        String token = UUID.randomUUID().toString();
        String redisKey = "idempotent:token:" + token;
        redisTemplate.opsForValue().set(redisKey, token, 5, TimeUnit.MINUTES);
        return ApiResult.success(token);
    }
}
