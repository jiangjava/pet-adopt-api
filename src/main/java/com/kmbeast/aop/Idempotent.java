package com.kmbeast.aop;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {
    /**
     * 令牌在请求中的位置，默认从 header 获取
     */
    String location() default "header";

    /**
     * 令牌的参数名，默认 "idempotentToken"
     */
    String key() default "idempotentToken";

    /**
     * 令牌过期时间（秒），默认5分钟
     */
    int expire() default 300;
}
