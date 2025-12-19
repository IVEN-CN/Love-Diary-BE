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
import java.util.*;
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
        String hashKey = record.getLink(); // 使用link作为hash key，确保每个邀请都是唯一的
        
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
            
            // 按创建时间降序排序（最新的在前）
            records.sort(Comparator.comparing(BindInviteRecord::getCreateTime, 
                    Comparator.nullsLast(Comparator.reverseOrder())));
            
            log.info("从Redis获取邀请记录列表: key={}, count={}", key, records.size());
        } catch (Exception e) {
            log.error("获取邀请记录失败: key={}, error={}", key, e.getMessage());
        }
        
        return records;
    }

    @Override
    public void deleteInviteRecord(Long toUserId, String link) {
        String key = INVITES_KEY_PREFIX + toUserId;
        redisTemplate.opsForHash().delete(key, link);
        log.info("从Redis删除邀请记录: key={}, hashKey={}", key, link);
    }

    @Override
    public void saveResponseRecord(BindResponseRecord record) {
        String key = RESPONSE_KEY_PREFIX + record.getFromUserId();
        String hashKey = record.getLink(); // 使用link作为hash key
        
        // 存储到Hash中
        redisTemplate.opsForHash().put(key, hashKey, record);
        
        // 设置整个Hash的过期时间为7天
        redisTemplate.expire(key, EXPIRE_SECONDS, TimeUnit.SECONDS);
        
        log.info("保存响应记录到Redis: key={}, hashKey={}, record={}", key, hashKey, record);
    }

    @Override
    public List<BindResponseRecord> getResponseRecords(Long fromUserId) {
        String key = RESPONSE_KEY_PREFIX + fromUserId;
        List<BindResponseRecord> records = new ArrayList<>();
        
        try {
            Map<Object, Object> allResponses = redisTemplate.opsForHash().entries(key);
            LocalDateTime now = LocalDateTime.now();
            
            for (Map.Entry<Object, Object> entry : allResponses.entrySet()) {
                try {
                    BindResponseRecord record = objectMapper.convertValue(entry.getValue(), BindResponseRecord.class);
                    
                    // 检查是否过期
                    if (record.getResponseTime() != null && 
                        record.getResponseTime().plusDays(EXPIRE_DAYS).isAfter(now)) {
                        records.add(record);
                    } else {
                        // 删除已过期的记录
                        String hashKey = entry.getKey().toString();
                        redisTemplate.opsForHash().delete(key, hashKey);
                        log.info("删除过期的响应记录: key={}, hashKey={}", key, hashKey);
                    }
                } catch (IllegalArgumentException e) {
                    log.error("解析响应记录失败: key={}, error={}", key, e.getMessage());
                }
            }
            
            // 按响应时间降序排序（最新的在前）
            records.sort(Comparator.comparing(BindResponseRecord::getResponseTime, 
                    Comparator.nullsLast(Comparator.reverseOrder())));
            
            log.info("从Redis获取响应记录列表: key={}, count={}", key, records.size());
        } catch (Exception e) {
            log.error("获取响应记录失败: key={}, error={}", key, e.getMessage());
        }
        
        return records;
    }

    @Override
    public Optional<BindResponseRecord> getResponseRecordByLink(Long fromUserId, String link) {
        String key = RESPONSE_KEY_PREFIX + fromUserId;
        try {
            Object value = redisTemplate.opsForHash().get(key, link);
            if (value != null) {
                BindResponseRecord record = objectMapper.convertValue(value, BindResponseRecord.class);
                log.info("从Redis获取响应记录: key={}, link={}", key, link);
                return Optional.of(record);
            }
        } catch (Exception e) {
            log.error("获取响应记录失败: key={}, link={}, error={}", key, link, e.getMessage());
        }
        return Optional.empty();
    }
}
