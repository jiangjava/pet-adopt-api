package com.kmbeast.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitCacheConfig {
    public static final String CACHE_EXCHANGE = "cache.exchange";
    public static final String CACHE_QUEUE = "cache.queue";
    public static final String CACHE_ROUTING_KEY = "cache.delete";

    @Bean
    public DirectExchange cacheExchange() {
        return new DirectExchange(CACHE_EXCHANGE, true, false); // 持久化
    }

    @Bean
    public Queue cacheQueue() {
        return new Queue(CACHE_QUEUE, true); // 持久化
    }

    @Bean
    public Binding cacheBinding() {
        return BindingBuilder.bind(cacheQueue())
                .to(cacheExchange())
                .with(CACHE_ROUTING_KEY);
    }
}
