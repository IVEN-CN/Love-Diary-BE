package com.iven.memo.handler;

import com.iven.memo.exceptions.GlobalException;
import com.iven.memo.models.Message.ResponseMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
class GlobalExceptionHandler {
    /**
     * 处理参数校验异常
     * @param ex 参数校验异常
     * @return 响应消息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseMessage<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        // 构建你的响应对象
        ResponseMessage<Void> resp = ResponseMessage.error(HttpStatus.BAD_REQUEST, errorMessage);
        // 用 ResponseEntity 包裹并指定 HTTP 状态码
        return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
    }

    /**
     * 处理全局自定义异常
     * @param ex 全局异常
     * @return 响应消息
     */
    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<ResponseMessage<Void>> handleGlobalException(GlobalException ex) {
        log.error(ex.getMessage(), ex);
        ResponseMessage<Void> resp = ResponseMessage.error(ex.getHttpStatus(), ex.getMessage());
        return new ResponseEntity<>(resp, ex.getHttpStatus());
    }

    /**
     * 处理其他未捕获的异常
     * @param ex 异常
     * @return 响应消息
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseMessage<Void>> handleException(Exception ex) {
        log.error(ex.getMessage(), ex);
        ResponseMessage<Void> resp = ResponseMessage.error(HttpStatus.INTERNAL_SERVER_ERROR, "服务器内部错误");
        return new ResponseEntity<>(resp, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
