package com.iven.memo.service;

import com.iven.memo.models.Message.BindInviteRecord;
import com.iven.memo.models.Message.BindResponseRecord;

import java.util.List;
import java.util.Optional;

/**
 * 绑定邀请Redis服务
 */
public interface BindInviteRedisService {
    /**
     * 保存邀请记录到Redis（7天过期）
     * @param record 邀请记录
     */
    void saveInviteRecord(BindInviteRecord record);

    /**
     * 获取用户收到的所有邀请记录
     * @param toUserId 被邀请用户ID
     * @return 邀请记录列表
     */
    List<BindInviteRecord> getInviteRecords(Long toUserId);

    /**
     * 删除特定的邀请记录
     * @param toUserId 被邀请用户ID
     * @param link 邀请短链（唯一标识）
     */
    void deleteInviteRecord(Long toUserId, String link);

    /**
     * 保存响应记录到Redis（7天过期）
     * @param record 响应记录
     */
    void saveResponseRecord(BindResponseRecord record);

    /**
     * 获取用户发出邀请的所有响应记录
     * @param fromUserId 邀请发起人用户ID
     * @return 响应记录列表
     */
    List<BindResponseRecord> getResponseRecords(Long fromUserId);

    /**
     * 根据link获取响应记录
     * @param fromUserId 邀请发起人用户ID
     * @param link 邀请短链
     * @return 响应记录
     */
    Optional<BindResponseRecord> getResponseRecordByLink(Long fromUserId, String link);
}
