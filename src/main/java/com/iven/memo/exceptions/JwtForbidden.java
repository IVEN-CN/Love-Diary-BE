package com.iven.memo.exceptions;

import org.springframework.http.HttpStatus;

public class JwtForbidden extends GlobalException {
    public JwtForbidden(String msg) {
        super(HttpStatus.FORBIDDEN, msg);
    }
}
