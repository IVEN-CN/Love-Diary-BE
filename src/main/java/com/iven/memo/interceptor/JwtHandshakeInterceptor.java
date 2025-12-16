package com.iven.memo.interceptor;

import com.iven.memo.exceptions.LoginFail;
import com.iven.memo.mapper.UserMapper;
import com.iven.memo.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        List<String> authHeaders = request.getHeaders().get("Authorization");

        if (authHeaders == null || authHeaders.isEmpty()) {
            return false; // 拒绝握手
        }

        String auth = authHeaders.get(0);
        if (!auth.startsWith("Bearer ")) {
            return false;
        }

        String jwt = auth.substring(7);
        Long userId = Long.valueOf(jwtUtil.validateToken(jwt));
        userMapper.findById(userId).orElseThrow(() -> new LoginFail("用户不存在或已被禁用"));
        attributes.put("userId", userId);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, @Nullable Exception exception) {

    }
}
