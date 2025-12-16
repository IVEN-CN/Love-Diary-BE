package com.iven.memo.service;

import com.iven.memo.models.DTO.User.BindInfoDTO;

public interface LoverBindEventListener {
    /**
     * 伴侣绑定事件
     * @param infoDTO 绑定信息DTO,包含发起绑定和被绑定的用户信息
     */
    void onLoverBindEvent(BindInfoDTO infoDTO);

    /**
     * 伴侣解绑事件
     * @param infoDTO 绑定信息DTO,包含发起绑定和被绑定的用户信息
     */
    void onLoverDebindEvent(BindInfoDTO infoDTO);
}
