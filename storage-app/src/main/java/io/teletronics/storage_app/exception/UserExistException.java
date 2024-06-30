package io.teletronics.storage_app.exception;

import org.springframework.http.HttpStatus;

import static io.teletronics.storage_app.exception.ErrorMessages.USER_EXIST;

public class UserExistException extends BaseException {

    public UserExistException() {
        this(USER_EXIST.getMessageKey(), HttpStatus.BAD_REQUEST);
    }

    public UserExistException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }
}
