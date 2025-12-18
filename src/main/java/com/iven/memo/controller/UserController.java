package com.iven.memo.controller;

import com.iven.memo.exceptions.LoginFail;
import com.iven.memo.models.DO.User;
import com.iven.memo.models.DTO.BindInvite.BindInviteRecordDTO;
import com.iven.memo.models.DTO.User.*;
import com.iven.memo.models.Message.ResponseMessage;
import com.iven.memo.service.UserService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
class UserController {
    private final UserService userService;

    @PostMapping("/login")
    public ResponseMessage<UserTokenResponseDTO> login(@RequestBody @Valid UserLoginRequestDTO requestDTO) {
        UserTokenResponseDTO responseDTO = userService.login(requestDTO.getUserName(), requestDTO.getPassword());
        return ResponseMessage.success(responseDTO);
    }

    @PostMapping("/extension")
    public ResponseMessage<UserTokenResponseDTO> extensionToken() {
        UserTokenResponseDTO responseDTO = userService.extension();
        return ResponseMessage.success(responseDTO);
    }

    @GetMapping("/jwt2user")
    public ResponseMessage<UserInfoDisplayDTO> getUserInfoDTO() {
        User currentUser = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        UserInfoDisplayDTO responseDTO = new UserInfoDisplayDTO();
        if (currentUser != null) {
            BeanUtils.copyProperties(currentUser, responseDTO);
            return ResponseMessage.success(responseDTO);
        } else {
            return ResponseMessage.error(new LoginFail("无法获取用户信息"));
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping()
    public ResponseMessage<UserInfoDisplayDTO> updateUserInfo(@RequestBody @Valid UserInfoUpdateDTO updateDTO) {
        UserInfoDisplayDTO responseDTO = userService.update(updateDTO);
        return ResponseMessage.success(responseDTO);
    }

    /**
     * 更新密码
     */
    @PutMapping("/pwd")
    public ResponseMessage<Void> updatePassword(@RequestBody @Valid UserPwdUpdateDTO pwdUpdateDTO) {
        userService.updatePwd(pwdUpdateDTO);
        return ResponseMessage.success();
    }

    /**
     * 获取伴侣信息
     */
    @GetMapping("/lover")
    public ResponseMessage<UserInfoDisplayDTO> getLoverInfo() {
        UserInfoDisplayDTO displayDTO = userService.getLoverInfo();
        return ResponseMessage.success(displayDTO);
    }

    /**
     * 绑定伴侣
     * @param userName 仅包含伴侣用户名的DTO
     * @return 绑定的伴侣的用户信息
     */
    @PutMapping("/lover")
    public ResponseMessage<Void> bindLover(@RequestBody @Valid OnlyUserNameDTO userName) {
        userService.bindLover(userName);
        return ResponseMessage.success("邀请已发送");
    }

    /**
     * 解绑伴侣
     */
    @DeleteMapping("/lover")
    public ResponseMessage<Void> deBindLover() {
        userService.deBindLover();
        return ResponseMessage.success("伴侣解绑成功");
    }

    /**
     * 接受绑定
     * @param link 短链链接
     */
    @PostMapping("/lover/accept/{link}")
    public ResponseMessage<Void> acceptBindLover(@PathVariable String link) {
        userService.acceptBindLover(link);
        return ResponseMessage.success("伴侣绑定成功");
    }

    /**
     * 拒绝邀请
     * @param link 短链链接
     */
    @DeleteMapping("/lover/reject/{link}")
    public ResponseMessage<Void> rejectBindLover(@PathVariable String link) {
        userService.rejectBindLover(link);
        return ResponseMessage.success("伴侣绑定邀请已拒绝");
    }

    /**
     * 获取绑定邀请记录列表
     * @return 绑定邀请记录列表
     */
    @GetMapping("/lover/invite")
    public ResponseMessage<List<BindInviteRecordDTO>> getBindInviteRecords() {
        List<BindInviteRecordDTO> records = userService.getBindInviteRecords();
        return ResponseMessage.success(records);
    }
}
