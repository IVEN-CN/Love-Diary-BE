package com.iven.memo.exceptions;

import org.springframework.http.HttpStatus;

public class PermissionDeny extends GlobalException {
    public PermissionDeny(String msg) {
        super(HttpStatus.FORBIDDEN, msg);
    }
}
