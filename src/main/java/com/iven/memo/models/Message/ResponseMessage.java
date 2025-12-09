package com.iven.memo.models.Message;

import com.iven.memo.exceptions.GlobalException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ResponseMessage <T> {
    private HttpStatus code;
    private String message;
    private T details;

    public static <T> ResponseMessage<T> success(String message, T details) {
        ResponseMessage<T> responseMessage = new ResponseMessage<T>();
        responseMessage.code = HttpStatus.OK;
        responseMessage.message = message;
        responseMessage.details = details;
        return responseMessage;
    }

    public static <T> ResponseMessage<T> success(T details) {
        return success("Success", details);
    }

    public static <T> ResponseMessage<T> success() {
        return success("Success", null);
    }

    public static <T> ResponseMessage<T> success(String message) {
        return success(message, null);
    }

    public static <T> ResponseMessage<T> error(String message) {
        ResponseMessage<T> responseMessage = new ResponseMessage<T>();
        responseMessage.code = HttpStatus.INTERNAL_SERVER_ERROR;
        responseMessage.message = message;
        responseMessage.details = null;
        return responseMessage;
    }

    public static <T> ResponseMessage<T> error(HttpStatus code, String message) {
        ResponseMessage<T> responseMessage = new ResponseMessage<T>();
        responseMessage.code = code;
        responseMessage.message = message;
        responseMessage.details = null;
        return responseMessage;
    }

    public static <T> ResponseMessage<T> error(GlobalException globalException) {
        ResponseMessage<T> responseMessage = new ResponseMessage<T>();
        responseMessage.code = globalException.getHttpStatus();
        responseMessage.message = globalException.getMessage();
        responseMessage.details = null;
        return responseMessage;
    }
}
