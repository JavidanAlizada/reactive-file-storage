package io.teletronics.storage_app.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Date;

public class BaseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private String message;
    @Getter
    private HttpStatus httpStatus;
    @Getter
    private Date date;

    public BaseException() {
    }

    public BaseException(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
        this.date = new Date();
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "BaseException{" +
                "message='" + message + '\'' +
                ", httpStatus=" + httpStatus +
                ", date=" + date +
                '}';
    }
}

