package com.iven.memo.service.impl;

import com.iven.memo.handler.CommonMessageWebSocketHandler;
import com.iven.memo.models.DTO.User.BindInfoDTO;
import com.iven.memo.models.Enumerate.WSMessageType;
import com.iven.memo.models.Message.LoverBindMessage;
import com.iven.memo.models.Message.WSMessage;
import com.iven.memo.service.LoverBindEventListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoverBindEventListenerImpl implements LoverBindEventListener {
    private final CommonMessageWebSocketHandler commonMessageWebSocketHandler;

    @Async
    @EventListener(condition = "#infoDTO.bindType == T(com.iven.memo.models.Enumerate.BindType).BIND")
    @Override
    public void onLoverBindEvent(BindInfoDTO infoDTO) {
        // 构造ws消息
        LoverBindMessage bindMessage = LoverBindMessage.builder()
                .link(infoDTO.getLink())
                .fromUserId(infoDTO.getFromUser().getId())
                .fromUserName(infoDTO.getFromUser().getUserName())
                .build();
        WSMessage<LoverBindMessage> wsMessage = WSMessage.<LoverBindMessage>builder()
                .messageType(com.iven.memo.models.Enumerate.WSMessageType.LOVER_BIND)
                .message(bindMessage)
                .build();

        // 发送消息
        try {
            commonMessageWebSocketHandler.sendMessage(infoDTO.getToUser().getId(), wsMessage);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(condition = "#infoDTO.bindType == T(com.iven.memo.models.Enumerate.BindType).DEBIND")
    @Override
    public void onLoverDebindEvent(BindInfoDTO infoDTO) {
        // 构造ws消息
        LoverBindMessage bindMessage = LoverBindMessage.builder()
                .fromUserId(infoDTO.getFromUser().getId())
                .fromUserName(infoDTO.getFromUser().getUserName())
                .build();
        WSMessage<LoverBindMessage> wsMessage = WSMessage.<LoverBindMessage>builder()
                .messageType(WSMessageType.LOVER_DEBIND)
                .message(bindMessage)
                .build();

        // 发送消息
        try {
            commonMessageWebSocketHandler.sendMessage(infoDTO.getToUser().getId(), wsMessage);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
