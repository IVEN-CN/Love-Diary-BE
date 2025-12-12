package com.iven.memo.exceptions;

import org.springframework.http.HttpStatus;

public class LoginFail extends GlobalException {
    public LoginFail(String msg) {
        super(HttpStatus.UNAUTHORIZED, msg);
    }
}
