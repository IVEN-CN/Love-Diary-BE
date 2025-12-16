package com.iven.memo.exceptions;

import org.springframework.http.HttpStatus;

public class DataAlreadyExist extends GlobalException {
    public DataAlreadyExist(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
