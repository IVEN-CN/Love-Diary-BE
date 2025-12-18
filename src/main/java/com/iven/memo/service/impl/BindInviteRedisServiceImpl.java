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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class BindInviteRedisServiceImpl implements BindInviteRedisService {
    private final RedisTemplate<String, Object> redisTemplate;
    
    // Redis键前缀
    private static final String INVITES_KEY_PREFIX = "bind:invites:to:";
    private static final String RESPONSE_KEY_PREFIX = "bind:response:to:";
    
    // 过期时间常量
    private static final int EXPIRE_DAYS = 7;
    private static final long EXPIRE_SECONDS = EXPIRE_DAYS * 24 * 60 * 60;
    private final ObjectMapper objectMapper;

    @Override
    public void saveInviteRecord(BindInviteRecord record) {
        String key = INVITES_KEY_PREFIX + record.getToUserId();
        String hashKey = String.valueOf(record.getFromUserId());
        
        // 存储到Hash中
        redisTemplate.opsForHash().put(key, hashKey, record);
        
        // 设置整个Hash的过期时间为7天
        redisTemplate.expire(key, EXPIRE_SECONDS, TimeUnit.SECONDS);
        
        log.info("保存邀请记录到Redis: key={}, hashKey={}, record={}", key, hashKey, record);
    }

    @Override
    public List<BindInviteRecord> getInviteRecords(Long toUserId) {
        String key = INVITES_KEY_PREFIX + toUserId;
        List<BindInviteRecord> records = new ArrayList<>();
        
        try {
            Map<Object, Object> allInvites = redisTemplate.opsForHash().entries(key);
            LocalDateTime now = LocalDateTime.now();
            
            for (Map.Entry<Object, Object> entry : allInvites.entrySet()) {
                try {
                    BindInviteRecord record = objectMapper.convertValue(entry.getValue(), BindInviteRecord.class);
                    
                    // 检查是否过期
                    if (record.getCreateTime() != null && 
                        record.getCreateTime().plusDays(EXPIRE_DAYS).isAfter(now)) {
                        records.add(record);
                    } else {
                        // 删除已过期的记录
                        String hashKey = entry.getKey().toString();
                        redisTemplate.opsForHash().delete(key, hashKey);
                        log.info("删除过期的邀请记录: key={}, hashKey={}", key, hashKey);
                    }
                } catch (IllegalArgumentException e) {
                    log.error("解析邀请记录失败: key={}, error={}", key, e.getMessage());
                }
            }
            
            log.info("从Redis获取邀请记录列表: key={}, count={}", key, records.size());
        } catch (Exception e) {
            log.error("获取邀请记录失败: key={}, error={}", key, e.getMessage());
        }
        
        return records;
    }

    @Override
    public void deleteInviteRecord(Long toUserId, Long fromUserId) {
        String key = INVITES_KEY_PREFIX + toUserId;
        String hashKey = String.valueOf(fromUserId);
        redisTemplate.opsForHash().delete(key, hashKey);
        log.info("从Redis删除邀请记录: key={}, hashKey={}", key, hashKey);
    }

    @Override
    public void saveResponseRecord(BindResponseRecord record) {
        String key = RESPONSE_KEY_PREFIX + record.getFromUserId();
        redisTemplate.opsForValue().set(key, record, EXPIRE_SECONDS, TimeUnit.SECONDS);
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
