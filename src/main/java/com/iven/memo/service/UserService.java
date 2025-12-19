package com.iven.memo.service;

import com.iven.memo.models.DTO.BindInvite.BindInviteRecordDTO;
import com.iven.memo.models.DTO.BindInvite.SystemMessageDTO;
import com.iven.memo.models.DTO.BindInvite.UnifiedSystemMessageDTO;
import com.iven.memo.models.DTO.User.*;

import java.util.List;

public interface UserService {
    UserTokenResponseDTO login(String username, String password);

    /**
     * token续期
     * @return 包含续期后的token的用户信息
     */
    UserTokenResponseDTO extension();

    /**
     * 更新用户信息
     * @param updateDTO 用户信息更新DTO
     * @return 更新后的用户信息展示DTO
     */
    UserInfoDisplayDTO update(UserInfoUpdateDTO updateDTO);

    /**
     * 更新用户密码
     * @param pwdUpdateDTO 用户密码更新DTO
     */
    void updatePwd(UserPwdUpdateDTO pwdUpdateDTO);

    /**
     * 获取情人用户信息
     * @return 伴侣的用户信息
     */
    UserInfoDisplayDTO getLoverInfo();

    /**
     * 绑定伴侣
     *
     * @param userName 包含伴侣的用户名
     * @return
     */
    void bindLover(OnlyUserNameDTO userName);

    /**
     * 解绑伴侣
     */
    void deBindLover();

    /**
     * 接受绑定伴侣请求
     * @param link 短链链接
     */
    void acceptBindLover(String link);

    /**
     * 拒绝绑定伴侣请求
     * @param link 短链链接
     */
    void rejectBindLover(String link);

    /**
     * 获取绑定邀请记录列表
     * @return 绑定邀请记录列表
     */
    List<BindInviteRecordDTO> getBindInviteRecords();

    /**
     * 获取系统消息（包括邀请消息和响应消息）
     * @return 系统消息DTO
     */
    SystemMessageDTO getSystemMessages();

    /**
     * 获取统一的系统消息列表（邀请消息和响应消息合并，按时间排序）
     * @return 统一系统消息列表
     */
    List<UnifiedSystemMessageDTO> getUnifiedSystemMessages();
}
