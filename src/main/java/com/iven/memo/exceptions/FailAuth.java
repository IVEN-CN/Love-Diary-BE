package com.iven.memo.exceptions;

import org.springframework.security.core.AuthenticationException;

public class FailAuth extends AuthenticationException {
    public FailAuth(String message) {
        super(message);
    }
}
