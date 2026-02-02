package com.example.computerassociation.controller;

import com.example.computerassociation.template.RedisStorageTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/redis")
public class RedisController {

    @Autowired
    private RedisStorageTemplate redisStorage;

    @PostMapping("/set")
    public void setValue(@RequestBody Map<String, Object> data) {
        String key = (String) data.get("key");
        Object value = data.get("value");
        // 设置带过期时间的键
        redisStorage.set(key, value, 1, TimeUnit.HOURS);
    }

    @GetMapping("/get/{key}")
    public Object getValue(@PathVariable String key) {
        return redisStorage.get(key, Object.class);
    }

    @PostMapping("/hash")
    public void setHash(@RequestBody Map<String, Object> data) {
        String key = (String) data.get("key");
        String hashKey = (String) data.get("hashKey");
        Object value = data.get("value");
        redisStorage.hSet(key, hashKey, value);
    }
}
