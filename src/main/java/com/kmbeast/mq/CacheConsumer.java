package com.kmbeast.mq;

import com.alibaba.fastjson2.JSON;
import com.kmbeast.config.RabbitCacheConfig;
import com.kmbeast.pojo.entity.CacheMessage;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class CacheConsumer {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @RabbitListener(queues = RabbitCacheConfig.CACHE_QUEUE)
    public void handleCacheDelete(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String messageBody = new String(message.getBody());

        try {
            // 解析消息
            CacheMessage cacheMessage = JSON.parseObject(messageBody, CacheMessage.class);
            String cacheKey = cacheMessage.getCacheKey();

            // 执行缓存删除
            redisTemplate.delete(cacheKey);
            log.info("缓存删除成功: {}", cacheKey);

            // 手动确认
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("缓存删除失败, 消息: {}", messageBody, e);
        }
    }
}

