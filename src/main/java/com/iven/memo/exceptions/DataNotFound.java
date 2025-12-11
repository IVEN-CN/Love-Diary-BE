package com.iven.memo.exceptions;

import org.springframework.http.HttpStatus;

public class DataNotFound extends GlobalException {
    public DataNotFound(String msg) {
        super(HttpStatus.NOT_FOUND, msg);
    }
}
