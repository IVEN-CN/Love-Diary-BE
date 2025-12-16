package com.iven.memo.handler;

import com.iven.memo.models.Message.WSMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommonMessageWebSocketHandler implements WebSocketHandler {
    // 存储用户id与会话的关系
    private static final Map<Long, WebSocketSession> userSessionMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userid = (Long) session.getAttributes().get("userId");
        userSessionMap.put(userid, session);
        log.info("用户 (Id) {} 已连接websocket", userid);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("websocket传输出现错误: {}", exception.getMessage(), exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        userSessionMap.remove(userId);
        log.warn("用户 (id) {} 断开websocket连接", userId);
    }

    @Override
    public boolean supportsPartialMessages() {
        // 关闭部分消息支持
        return false;
    }

    /**
     * 发送消息给指定用户
     *
     * @param userId  用户ID
     * @param message 消息内容
     * @throws Exception 发送消息异常
     */
    public void sendMessage(Long userId, WebSocketMessage<?> message) throws Exception {
        WebSocketSession session = userSessionMap.get(userId);
        if (session != null && session.isOpen()) {
            log.info("发送消息给用户 (id) {}: {}", userId, message.getPayload());
            session.sendMessage(message);
        }
    }

    /**
     * 发送JSON消息给指定用户
     *
     * @param userId  用户ID
     * @param message 消息内容对象
     * @throws Exception 发送消息异常
     */
    public void sendMessage(Long userId, WSMessage<?> message) throws Exception {
        TextMessage textMessage = new TextMessage(objectMapper.writeValueAsString(message));
        sendMessage(userId, textMessage);
    }
}
