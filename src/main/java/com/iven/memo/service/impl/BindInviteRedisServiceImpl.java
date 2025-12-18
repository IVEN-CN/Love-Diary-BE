package com.iven.memo.service.impl;

import com.iven.memo.models.Message.BindInviteRecord;
import com.iven.memo.models.Message.BindResponseRecord;
import com.iven.memo.service.BindInviteRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BindInviteRedisServiceImpl implements BindInviteRedisService {
    private final RedisTemplate<String, Object> redisTemplate;
    
    // Redis键前缀
    private static final String INVITE_KEY_PREFIX = "bind:invite:to:";
    private static final String RESPONSE_KEY_PREFIX = "bind:response:to:";
    
    // 7天过期时间
    private static final Duration EXPIRE_DURATION = Duration.ofDays(7);
    private final ObjectMapper objectMapper;

    @Override
    public void saveInviteRecord(BindInviteRecord record) {
        String key = INVITE_KEY_PREFIX + record.getToUserId();
        redisTemplate.opsForValue().set(key, record, EXPIRE_DURATION);
        log.info("保存邀请记录到Redis: key={}, record={}", key, record);
    }

    @Override
    public Optional<BindInviteRecord> getInviteRecord(Long toUserId) {
        String key = INVITE_KEY_PREFIX + toUserId;
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                BindInviteRecord record = objectMapper.convertValue(value, BindInviteRecord.class);
                log.info("从Redis获取邀请记录: key={}", key);
                return Optional.of(record);
            }
        } catch (ClassCastException e) {
            log.error("Redis值类型转换失败: key={}, error={}", key, e.getMessage());
        }
        log.info("Redis中未找到邀请记录: key={}", key);
        return Optional.empty();
    }

    @Override
    public void deleteInviteRecord(Long toUserId) {
        String key = INVITE_KEY_PREFIX + toUserId;
        redisTemplate.delete(key);
        log.info("从Redis删除邀请记录: key={}", key);
    }

    @Override
    public void saveResponseRecord(BindResponseRecord record) {
        String key = RESPONSE_KEY_PREFIX + record.getFromUserId();
        redisTemplate.opsForValue().set(key, record, EXPIRE_DURATION);
        log.info("保存响应记录到Redis: key={}, record={}", key, record);
    }

    @Override
    public Optional<BindResponseRecord> getResponseRecord(Long fromUserId) {
        String key = RESPONSE_KEY_PREFIX + fromUserId;
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                BindResponseRecord record = (BindResponseRecord) value;
                log.info("从Redis获取响应记录: key={}", key);
                return Optional.of(record);
            }
        } catch (ClassCastException e) {
            log.error("Redis值类型转换失败: key={}, error={}", key, e.getMessage());
        }
        log.info("Redis中未找到响应记录: key={}", key);
        return Optional.empty();
    }

    @Override
    public void deleteResponseRecord(Long fromUserId) {
        String key = RESPONSE_KEY_PREFIX + fromUserId;
        redisTemplate.delete(key);
        log.info("从Redis删除响应记录: key={}", key);
    }
}
