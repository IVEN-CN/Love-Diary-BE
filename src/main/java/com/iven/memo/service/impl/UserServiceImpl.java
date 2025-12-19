package com.iven.memo.service.impl;

import com.iven.memo.exceptions.*;
import com.iven.memo.handler.CommonMessageWebSocketHandler;
import com.iven.memo.mapper.BindInviteMapper;
import com.iven.memo.mapper.UserMapper;
import com.iven.memo.models.DO.BindInvite;
import com.iven.memo.models.DO.User;
import com.iven.memo.models.DTO.BindInvite.BindInviteRecordDTO;
import com.iven.memo.models.DTO.BindInvite.BindInviteRequest;
import com.iven.memo.models.DTO.BindInvite.BindResponseRecordDTO;
import com.iven.memo.models.DTO.BindInvite.SystemMessageDTO;
import com.iven.memo.models.DTO.BindInvite.UnifiedSystemMessageDTO;
import com.iven.memo.models.DTO.User.*;
import com.iven.memo.models.Enumerate.BindType;
import com.iven.memo.models.Enumerate.WSMessageType;
import com.iven.memo.models.Message.BindInviteRecord;
import com.iven.memo.models.Message.BindResponseRecord;
import com.iven.memo.models.Message.LoverBindMessage;
import com.iven.memo.models.Message.WSMessage;
import com.iven.memo.service.BindInviteRedisService;
import com.iven.memo.service.UserService;
import com.iven.memo.utils.Base62Utils;
import com.iven.memo.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Base64Util;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final CommonMessageWebSocketHandler commonMessageWebSocketHandler;
    private final BindInviteMapper inviteMapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final BindInviteRedisService bindInviteRedisService;
    
    // 消息类型常量
    private static final String MESSAGE_TYPE_INVITE = "INVITE";
    private static final String MESSAGE_TYPE_RESPONSE = "RESPONSE";

    @Override
    public UserTokenResponseDTO login(String username, String password) {
        Optional<User> userOptional = userMapper.findByNameAndPassword(username, password);
        if (userOptional.isEmpty()) {
            throw new LoginFail("登录失败，用户名或密码错误");
        }
        return UserTokenResponseDTO.builder()
                .userName(userOptional.get().getUserName())
                .nickName(userOptional.get().getNickName())
                .birthday(userOptional.get().getBirthday())
                .token(jwtUtil.generateToken(userOptional.get().getId()))
                .build();
    }

    @Override
    public UserTokenResponseDTO extension() {
        //从上下文拿到User
        User user = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();

        // 生成新的token并返回用户信息
        if (user != null) {
            return UserTokenResponseDTO.builder()
                    .userName(user.getUserName())
                    .nickName(user.getNickName())
                    .birthday(user.getBirthday())
                    .token(jwtUtil.generateToken(user.getId()))
                    .build();
        } else {
            throw new LoginFail("用户未登录，无法续期");
        }
    }

    @Override
    public UserInfoDisplayDTO update(UserInfoUpdateDTO updateDTO) {
        User user = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        if (user != null) {
            BeanUtils.copyProperties(updateDTO, user);
            log.info("更新用户信息: {}", user);
            int influence = userMapper.updateById(user);
            log.info("mybatis update User {}", user);

            if (influence <= 0) {
                throw new GlobalException("更新用户信息时，数据库操作失败，mybatis影响行数为0");
            }

            UserInfoDisplayDTO userInfoDisplayDTO = new UserInfoDisplayDTO();
            BeanUtils.copyProperties(user, userInfoDisplayDTO);

            return userInfoDisplayDTO;
        } else {
            throw new LoginFail("用户未登录，无法更新");
        }
    }

    @Override
    public void updatePwd(UserPwdUpdateDTO pwdUpdateDTO) {
        User user = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        if (user != null) {
            // 校验旧密码
            if (!user.getPassword().equals(pwdUpdateDTO.getOriPwd())) {
                throw new LoginFail("旧密码错误，无法更新密码");
            }

            // 更新为新密码
            user.setPassword(pwdUpdateDTO.getNewPwd());
            log.info("更新用户密码: {}", user);
            int influence = userMapper.updateById(user);
            log.info("mybatis update User {}", user);

            if (influence <= 0) {
                throw new GlobalException("更新用户密码时，数据库操作失败，mybatis影响行数为0");
            }
        } else {
            throw new LoginFail("用户未登录，无法更新");
        }
    }

    @Override
    public UserInfoDisplayDTO getLoverInfo() {
        User currentUser = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        if (currentUser != null) {
            Long loverId =  currentUser.getLoverId();
            if (loverId == null) {
                throw new DataNotFound("当前用户还没有绑定伴侣");
            }

            Optional<User> loverOptional = userMapper.findById(loverId);
            if (loverOptional.isPresent()) {
                User lover =  loverOptional.get();
                return new UserInfoDisplayDTO(lover);
            } else {
                log.error("用户 {} 的情人不存在，但是绑定了id", currentUser);
                throw new DataNotFound("伴侣未找到");
            }
        } else {
            throw new LoginFail("用户未登录，无法更新");
        }
    }

    @Override
    public void bindLover(OnlyUserNameDTO userName) {
        // 取出当前user
        User currentUser = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        if (currentUser == null) {
            throw new LoginFail("用户未登录，无法绑定伴侣");
        }

        // 取出伴侣user
        Optional<User> loverOptional = userMapper.findByLoverUserName(userName.getUsername());
        if (loverOptional.isEmpty()) {
            throw new DataNotFound("未找到该伴侣用户");
        }

        User lover = loverOptional.get();
        if (lover.getLoverId() != null) {
            throw new DataAlreadyExist("该用户已有伴侣，无法绑定");
        }

        // 生成邀请信息
        LocalDateTime expireTime = LocalDateTime.now().plusDays(3);
        BindInvite invite = BindInvite.builder()
                .fromUserId(currentUser.getId())
                .toUserId(lover.getId())
                .expireTime(expireTime)        // 3天后过期
                .build();
        inviteMapper.insert(invite);
        log.info("生成伴侣绑定邀请: {}", invite);

        // 生成短链
        Long inviteId = invite.getId();
        if (inviteId == null) {
            log.error("生成伴侣绑定邀请失败，邀请ID为空: {}", invite);
            throw new RuntimeException("生成伴侣绑定邀请失败，mybatis没有回写id");
        }
        String base62Id = Base62Utils.encode(inviteId);
        log.info("生成伴侣绑定邀请短链: {}", base62Id);

        // 保存邀请记录到Redis（7天过期）
        BindInviteRecord inviteRecord = BindInviteRecord.builder()
                .fromUserId(currentUser.getId())
                .fromUserName(currentUser.getUserName())
                .toUserId(lover.getId())
                .link(base62Id)
                .createTime(LocalDateTime.now())
                .build();
        bindInviteRedisService.saveInviteRecord(inviteRecord);

        // 发布绑定事件
        BindInfoDTO bindInfo = BindInfoDTO.builder()
                .fromUser(currentUser)
                .toUser(lover)
                .link(base62Id)
                .bindType(BindType.BIND)
                .build();
        applicationEventPublisher.publishEvent(bindInfo);
    }

    @Override
    @Transactional
    public void deBindLover() {
        // 取出当前user
        User currentUser = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        if (currentUser == null) {
            throw new LoginFail("用户未登录，无法解绑伴侣");
        }

        // 解绑不需要伴侣同意，直接解绑
        Long loverId = currentUser.getLoverId();
        if (loverId == null) {
            throw new DataNotFound("当前用户还没有绑定伴侣");
        }

        Optional<User> loverOptional = userMapper.findById(loverId);
        if (loverOptional.isEmpty()) {
            log.error("用户 {} 的情人不存在，但是绑定了id", currentUser);
            throw new DataNotFound("伴侣未找到");
        }

        // 发布解绑事件
        BindInfoDTO bindInfo = BindInfoDTO.builder()
                .fromUser(currentUser)
                .toUser(loverOptional.get())
                .link(null)
                .bindType(BindType.DEBIND)
                .build();
        applicationEventPublisher.publishEvent(bindInfo);

        // 清除当前用户的loverId
        currentUser.setLoverId(null);
        int influence1 = userMapper.updateById(currentUser);
        log.info("mybatis update User {}", currentUser);
        if (influence1 <= 0) {
            throw new GlobalException("解绑伴侣时，更新当前用户失败，mybatis影响行数为0");
        }

        // 清除伴侣的loverId
        User lover = loverOptional.get();
        lover.setLoverId(null);
        int influence2 = userMapper.updateById(lover);
        log.info("mybatis update User {}", lover);
        if (influence2 <= 0) {
            throw new GlobalException("解绑伴侣时，更新伴侣用户失败，mybatis影响行数为0");
        }
    }

    @Override
    @Transactional
    public void acceptBindLover(String link) {
        // 取出当前user
        User currentUser = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        if (currentUser == null) {
            throw new LoginFail("用户未登录，无法解绑伴侣");
        }

        // 找出邀请码id
        Long inviteId = Base62Utils.decode(link);
        Optional<BindInvite> inviteOptional = inviteMapper.findById(inviteId);
        if (inviteOptional.isEmpty()) {
            throw new DataNotFound("绑定邀请不存在或已过期");
        }

        BindInvite invite = inviteOptional.get();
        if (!invite.getToUserId().equals(currentUser.getId())) {
            throw new PermissionDeny("绑定邀请的接收用户与当前用户不匹配");
        }
        // 使用邀请码
        int influence = inviteMapper.acceptInvite(inviteId);
        if (influence <= 0) {
            throw new GlobalException("接受伴侣绑定时，使用邀请码失败，mybatis影响行数为0");
        }

        // 取出lover
        Optional<User> loverOptional = userMapper.findById(invite.getFromUserId());
        if (loverOptional.isEmpty()) {
            log.error("绑定邀请的发送用户不存在: {}", invite);
            throw new DataNotFound("绑定邀请的发送用户不存在");
        }
        User lover = loverOptional.get();
        lover.setLoverId(invite.getToUserId());
        int influenceLoverUserUpdate = userMapper.updateById(lover);
        if (influenceLoverUserUpdate <= 0) {
            throw new GlobalException("接受伴侣绑定时，更新伴侣用户失败，mybatis影响行数为0");
        }

        // 更新当前用户的loverId
        currentUser.setLoverId(invite.getFromUserId());
        int influenceCurrentUserUpdate = userMapper.updateById(currentUser);
        if (influenceCurrentUserUpdate <= 0) {
            throw new GlobalException("接受伴侣绑定时，更新当前用户失败，mybatis影响行数为0");
        }

        // 保存响应记录到Redis（7天过期）
        BindResponseRecord responseRecord = BindResponseRecord.builder()
                .fromUserId(invite.getFromUserId())
                .link(link)  // 添加link用于标识是哪个邀请被响应
                .responseUserId(currentUser.getId())
                .responseUserName(currentUser.getUserName())
                .accepted(true)
                .responseTime(LocalDateTime.now())
                .build();
        bindInviteRedisService.saveResponseRecord(responseRecord);

        // 不删除邀请记录，保留供被邀请人查看响应状态
        // bindInviteRedisService.deleteInviteRecord(currentUser.getId(), link);

        // 发送WebSocket消息通知邀请发起人
        sendBindResponseMessage(invite.getFromUserId(), currentUser, WSMessageType.BIND_ACCEPT);

        // 拒绝其他的未处理邀请
        // XXX 可以改为异步处理
        List<BindInvite> otherInvites = inviteMapper.findAllUnusedByToUserId(currentUser.getId());
        for (BindInvite otherInvite : otherInvites) {
            if (!otherInvite.getFromUserId().equals(invite.getFromUserId())) {
                // 如果找出来的邀请不是当前接受的邀请的发起人发起的，就拒绝掉
                rejectBindLover(currentUser, Base62Utils.encode(otherInvite.getId()));
            } else {
                // 如果是当前接受的邀请的发起人发起的，就设置为已使用，避免重复使用
                inviteMapper.useInvite(otherInvite.getId());
            }
        }
    }

    @Override
    public void rejectBindLover(String link) {
        // 取出当前user
        User currentUser = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        if (currentUser == null) {
            throw new LoginFail("用户未登录，无法解绑伴侣");
        }
        rejectBindLover(currentUser, link);
    }

    private void rejectBindLover(User currentUser, String link) {
        // 找出邀请码
        Long inviteId = Base62Utils.decode(link);
        Optional<BindInvite> inviteOptional = inviteMapper.findById(inviteId);
        if (inviteOptional.isEmpty()) {
            throw new DataNotFound("绑定邀请不存在或已过期");
        }

        BindInvite invite = inviteOptional.get();
        
        // 验证权限
        if (!invite.getToUserId().equals(currentUser.getId())) {
            throw new PermissionDeny("绑定邀请的接收用户与当前用户不匹配");
        }
        
        // 将邀请码设置为已使用
        int influence = inviteMapper.rejectInvite(inviteId);
        if (influence <= 0) {
            throw new GlobalException("拒绝伴侣绑定时，使用邀请码失败，mybatis影响行数为0");
        }

        // 保存响应记录到Redis（7天过期）
        BindResponseRecord responseRecord = BindResponseRecord.builder()
                .fromUserId(invite.getFromUserId())
                .link(link)  // 添加link用于标识是哪个邀请被响应
                .responseUserId(currentUser.getId())
                .responseUserName(currentUser.getUserName())
                .accepted(false)
                .responseTime(LocalDateTime.now())
                .build();
        bindInviteRedisService.saveResponseRecord(responseRecord);

        // 不删除邀请记录，保留供被邀请人查看响应状态
        // bindInviteRedisService.deleteInviteRecord(currentUser.getId(), link);

        // 发送WebSocket消息通知邀请发起人
        sendBindResponseMessage(invite.getFromUserId(), currentUser, WSMessageType.BIND_REJECT);
    }

    /**
     * 发送绑定响应WebSocket消息的辅助方法
     * @param targetUserId 目标用户ID
     * @param responseUser 响应用户
     * @param messageType 消息类型（BIND_ACCEPT或BIND_REJECT）
     */
    private void sendBindResponseMessage(Long targetUserId, User responseUser, WSMessageType messageType) {
        LoverBindMessage message = LoverBindMessage.builder()
                .fromUserId(responseUser.getId())
                .fromUserName(responseUser.getUserName())
                .link(null)
                .build();
        WSMessage<LoverBindMessage> wsMessage = WSMessage.<LoverBindMessage>builder()
                .messageType(messageType)
                .message(message)
                .build();
        
        try {
            commonMessageWebSocketHandler.sendMessage(targetUserId, wsMessage);
            log.info("发送{}消息给用户: {}", messageType, targetUserId);
        } catch (Exception e) {
            log.error("发送{}消息失败: {}", messageType, e.getMessage(), e);
        }
    }

    /**
     * 将邀请记录转换为DTO，并检查响应状态
     */
    private BindInviteRecordDTO convertToInviteDTO(BindInviteRecord inviteRecord) {
        // 检查该邀请是否有响应记录
        Optional<BindResponseRecord> responseOpt = 
            bindInviteRedisService.getResponseRecordByLink(
                inviteRecord.getFromUserId(), 
                inviteRecord.getLink()
            );
        
        return BindInviteRecordDTO.builder()
                .fromUserId(inviteRecord.getFromUserId())
                .fromUserName(inviteRecord.getFromUserName())
                .link(inviteRecord.getLink())
                .createTime(inviteRecord.getCreateTime())
                .hasResponse(responseOpt.isPresent())
                .accepted(responseOpt.map(BindResponseRecord::isAccepted).orElse(null))
                .build();
    }

    @Override
    public List<BindInviteRecordDTO> getBindInviteRecords() {
        // 取出当前user
        User currentUser = (User) Objects.requireNonNull(
                SecurityContextHolder.getContext().getAuthentication()).getPrincipal();

        // 查询当前用户收到的所有邀请记录
        List<BindInviteRecord> inviteRecords = bindInviteRedisService.getInviteRecords(currentUser.getId());
        
        // 转换为DTO列表
        return inviteRecords.stream()
                .map(this::convertToInviteDTO)
                .toList();
    }

    @Override
    public SystemMessageDTO getSystemMessages() {
        // 取出当前user
        User currentUser = (User) Objects.requireNonNull(
                SecurityContextHolder.getContext().getAuthentication()).getPrincipal();

        // 获取邀请消息（当前用户作为被邀请人收到的邀请）
        List<BindInviteRecord> inviteRecords = bindInviteRedisService.getInviteRecords(currentUser.getId());
        List<BindInviteRecordDTO> inviteMessages = inviteRecords.stream()
                .map(this::convertToInviteDTO)
                .toList();

        // 获取响应消息（当前用户作为邀请发起人收到的响应）
        List<BindResponseRecord> responseRecords = bindInviteRedisService.getResponseRecords(currentUser.getId());
        List<BindResponseRecordDTO> responseMessages = responseRecords.stream()
                .map(responseRecord -> BindResponseRecordDTO.builder()
                        .responseUserId(responseRecord.getResponseUserId())
                        .responseUserName(responseRecord.getResponseUserName())
                        .link(responseRecord.getLink())
                        .accepted(responseRecord.isAccepted())
                        .responseTime(responseRecord.getResponseTime())
                        .build())
                .toList();

        return SystemMessageDTO.builder()
                .inviteMessages(inviteMessages)
                .responseMessages(responseMessages)
                .build();
    }

    @Override
    public List<UnifiedSystemMessageDTO> getUnifiedSystemMessages() {
        // 取出当前user
        User currentUser = (User) Objects.requireNonNull(
                SecurityContextHolder.getContext().getAuthentication()).getPrincipal();

        List<UnifiedSystemMessageDTO> allMessages = new ArrayList<>();

        // 获取邀请消息（当前用户作为被邀请人收到的邀请）
        List<BindInviteRecord> inviteRecords = bindInviteRedisService.getInviteRecords(currentUser.getId());
        for (BindInviteRecord inviteRecord : inviteRecords) {
            // 检查该邀请是否有响应记录
            Optional<BindResponseRecord> responseOpt = 
                bindInviteRedisService.getResponseRecordByLink(
                    inviteRecord.getFromUserId(), 
                    inviteRecord.getLink()
                );
            
            UnifiedSystemMessageDTO message = UnifiedSystemMessageDTO.builder()
                    .messageType(MESSAGE_TYPE_INVITE)
                    .fromUserId(inviteRecord.getFromUserId())
                    .fromUserName(inviteRecord.getFromUserName())
                    .link(inviteRecord.getLink())
                    .time(inviteRecord.getCreateTime())
                    .hasResponse(responseOpt.isPresent())
                    .accepted(responseOpt.map(BindResponseRecord::isAccepted).orElse(null))
                    .build();
            allMessages.add(message);
        }

        // 获取响应消息（当前用户作为邀请发起人收到的响应）
        List<BindResponseRecord> responseRecords = bindInviteRedisService.getResponseRecords(currentUser.getId());
        for (BindResponseRecord responseRecord : responseRecords) {
            UnifiedSystemMessageDTO message = UnifiedSystemMessageDTO.builder()
                    .messageType(MESSAGE_TYPE_RESPONSE)
                    .responseUserId(responseRecord.getResponseUserId())
                    .responseUserName(responseRecord.getResponseUserName())
                    .link(responseRecord.getLink())
                    .time(responseRecord.getResponseTime())
                    .accepted(responseRecord.isAccepted())
                    .build();
            allMessages.add(message);
        }

        // 按时间降序排序（最新的在前）
        allMessages.sort(Comparator.comparing(UnifiedSystemMessageDTO::getTime, 
                Comparator.nullsLast(Comparator.reverseOrder())));

        return allMessages;
    }
}
