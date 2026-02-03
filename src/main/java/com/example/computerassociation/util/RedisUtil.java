package com.example.computerassociation.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis工具类，用于缓存操作
 * 存储验证码、令牌等临时数据
 */
@Component
public class RedisUtil {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 设置字符串值
     * @param key 键
     * @param value 值
     */
    public void setString(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置带过期时间的字符串值
     * @param key 键
     * @param value 值
     * @param timeout 过期时间
     * @param unit 时间单位
     */
    public void setString(String key, String value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 获取字符串值
     * @param key 键
     * @return 值
     */
    public String getString(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除指定键
     * @param key 键
     */
    public Boolean deleteKey(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 判断键是否存在
     * @param key 键
     * @return 是否存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }
}
