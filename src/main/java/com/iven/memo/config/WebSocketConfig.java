package com.iven.memo.config;

import com.iven.memo.handler.CommonMessageWebSocketHandler;
import com.iven.memo.interceptor.JwtHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
class WebSocketConfig implements WebSocketConfigurer {
    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;
    private final CommonMessageWebSocketHandler commonMessageWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(commonMessageWebSocketHandler, "/ws/common-message")
                .addInterceptors(jwtHandshakeInterceptor)
                .setAllowedOrigins("*");
    }
}
