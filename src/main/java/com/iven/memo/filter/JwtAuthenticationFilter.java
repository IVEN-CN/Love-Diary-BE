package com.iven.memo.filter;

import com.iven.memo.config.entryPoint.FailAuthEntryPoint;
import com.iven.memo.config.properties.AllowRequestWithoutAuthProperties;
import com.iven.memo.exceptions.FailAuth;
import com.iven.memo.mapper.UserMapper;
import com.iven.memo.models.DO.User;
import com.iven.memo.utils.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final AllowRequestWithoutAuthProperties allowRequestWithoutAuthProperties;
    private final PathMatcher pathMatcher = new AntPathMatcher();
    private final JwtUtil jwtUtil;
    private final FailAuthEntryPoint failAuthEntryPoint;
    private final UserMapper userMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String requestUrl = request.getRequestURI();    // 获取请求的URL
        String requestMethod = request.getMethod();     // 获取请求的方法

        // 取出Authorization头
        String authHeader = request.getHeader("Authorization");
        boolean validAuthHeader = authHeader != null && authHeader.startsWith("Bearer ");

        // 放行不需要过滤的URL（使用 AntPathMatcher 支持通配符）
        boolean skipGet = "GET".equalsIgnoreCase(requestMethod) &&
                allowRequestWithoutAuthProperties.getGet().stream()
                        .anyMatch(pattern -> pathMatcher.match(pattern, requestUrl));

        boolean skipPost = "POST".equalsIgnoreCase(requestMethod) &&
                allowRequestWithoutAuthProperties.getPost().stream()
                        .anyMatch(pattern -> pathMatcher.match(pattern, requestUrl));

        // 需要放行且没有Authentication头的请求，直接放行
        if ((skipGet || skipPost) && !validAuthHeader) {
            log.info("无需认证请求，直接放行: {} {}", request.getMethod(), request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }

        // 处理JWT认证
        if (validAuthHeader) {
            String token = authHeader.substring(7);
            try {
                // 取出用户id
                String userId = jwtUtil.validateToken(token);

                // 根据用户id查找用户实体
                Optional<User> userOptional = userMapper.findById(Long.valueOf(userId));
                if (userOptional.isEmpty()) {
                    log.error("用户不存在: {}", userId);
                    failAuthEntryPoint.commence(request, response, new FailAuth("用户不存在"));
                    return;
                }

                // 将安全上下文信息设置为用户实体
                User user = userOptional.get();
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        java.util.Collections.singletonList(new SimpleGrantedAuthority("USER"))
                ));

            } catch (ExpiredJwtException jwtException) {
                log.error("Token已过期: {}", jwtException.getMessage());
                failAuthEntryPoint.commence(request, response, new FailAuth(jwtException.getMessage()), HttpStatus.UNAUTHORIZED);
                return;
            } catch (RuntimeException e) {
                log.error("Token验证失败: {}", e.getMessage());
                failAuthEntryPoint.commence(request, response, new FailAuth(e.getMessage()));
                return;
            }
        } else {
            log.error("请求缺少Authorization头或格式错误: {}", authHeader);
            failAuthEntryPoint.commence(request, response, new FailAuth("请求缺少Authorization头或格式错误"));
            return;
        }
        filterChain.doFilter(request, response);
    }
}
