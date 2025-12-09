package com.iven.memo.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class GlobalException extends RuntimeException {
    private final HttpStatus httpStatus;

    public int getStatusCode() {
        return httpStatus.value();
    }

    /**
     * 全局异常，默认为500错误
     * @param message 异常信息
     */
    public GlobalException(String message) {
        super(message);
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    /**
     * 全局异常
     * @param httpStatus HTTP状态码
     * @param message 异常信息
     */
    public GlobalException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }
}
