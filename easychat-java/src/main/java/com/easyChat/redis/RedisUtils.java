package com.easyChat.redis;

import com.easyChat.constants.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class RedisUtils {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    /**
     * 设置缓存值并指定过期时间（以秒为单位）
     *
     * @param key     键
     * @param value   值
     * @param timeout 过期时间（秒）
     */
    public void setex(String key, Object value, long timeout) {
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
    }

    /**
     * 设置缓存值并指定过期时间（自定义时间单位）
     *
     * @param key      键
     * @param value    值
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     */
    public void setex(String key, Object value, long timeout, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }
    /**
     * 设置缓存值
     *
     * @param key   键
     * @param value 值
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置缓存值并指定过期时间
     *
     * @param key      键
     * @param value    值
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     */
    public void set(String key, Object value, long timeout, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    /**
     * 获取缓存值
     *
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除缓存键
     *
     * @param key 键
     * @return 是否删除成功
     */
    public boolean delete(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    /**
     * 判断缓存键是否存在
     *
     * @param key 键
     * @return 是否存在
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 设置缓存键的过期时间
     *
     * @param key      键
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     * @return 是否设置成功
     */
    public boolean expire(String key, long timeout, TimeUnit timeUnit) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, timeUnit));
    }

    /**
     * 获取缓存键的剩余过期时间
     *
     * @param key      键
     * @param timeUnit 时间单位
     * @return 剩余过期时间
     */
    public Long getExpire(String key, TimeUnit timeUnit) {
        return redisTemplate.getExpire(key, timeUnit);
    }

    /**
     * 递增操作
     *
     * @param key   键
     * @param delta 递增步长
     * @return 递增后的值
     */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 递减操作
     *
     * @param key   键
     * @param delta 递减步长
     * @return 递减后的值
     */
    public Long decrement(String key, long delta) {
        return redisTemplate.opsForValue().decrement(key, delta);
    }

    public void lpushAll(String key, List<String> values, long timeout) {
        ListOperations<String, Object> listOps = redisTemplate.opsForList();
        listOps.leftPushAll(key, values); // 将值推入列表
        redisTemplate.expire(key, timeout, TimeUnit.SECONDS); // 设置过期时间
    }

    public List<String> getQueueList(String userId) {
        String key = Constants.REDIS_KEY_USER_CONTACT + userId;
        ListOperations<String, Object> listOps = redisTemplate.opsForList();

        List<Object> redisData = listOps.range(key, 0, -1);
        List<String> contactList = new ArrayList<>();

        if (redisData != null) {
            for (Object item : redisData) {
                if (item instanceof List) {
                    // 如果是列表，提取其中的元素
                    List<?> innerList = (List<?>) item;
                    for (Object innerItem : innerList) {
                        contactList.add(innerItem.toString());
                    }
                } else {
                    contactList.add(item.toString());
                }
            }
        }

        return contactList;
    }

    /**
     * 将单个值推入列表头部，并设置过期时间（以秒为单位）
     *
     * @param key     键
     * @param value   值
     * @param timeout 过期时间（秒）
     */
    public void lpush(String key, Object value, long timeout) {
        ListOperations<String, Object> listOps = redisTemplate.opsForList();
        listOps.leftPush(key, value);
        redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 将单个值推入列表头部，并设置自定义单位的过期时间
     *
     * @param key      键
     * @param value    值
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     */
    public void lpush(String key, Object value, long timeout, TimeUnit timeUnit) {
        ListOperations<String, Object> listOps = redisTemplate.opsForList();
        listOps.leftPush(key, value);
        redisTemplate.expire(key, timeout, timeUnit);
    }

    /**
     * 将单个值推入列表头部（不设置过期时间）
     *
     * @param key   键
     * @param value 值
     */
    public void lpush(String key, Object value) {
        redisTemplate.opsForList().leftPush(key, value);
    }

}