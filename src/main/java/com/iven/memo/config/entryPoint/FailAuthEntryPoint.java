package com.iven.memo.config.entryPoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iven.memo.exceptions.FailAuth;
import com.iven.memo.models.Message.ResponseMessage;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class FailAuthEntryPoint implements AuthenticationEntryPoint {
    ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void commence(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull AuthenticationException authException) throws IOException, ServletException {
        commence(request, response, authException, HttpStatus.FORBIDDEN);
    }

    public void commence(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull AuthenticationException authException, @NotNull HttpStatus httpStatus) throws IOException {
        response.setStatus(httpStatus.value());
        response.setContentType("application/json;charset=UTF-8");
        ResponseMessage<Object> responseMessage = ResponseMessage.error(HttpStatus.FORBIDDEN, authException.getMessage());
        response.getWriter().write(objectMapper.writeValueAsString(responseMessage));
    }
}
