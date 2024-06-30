package io.teletronics.storage_app.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BaseException {

    public UserNotFoundException() {
        this(ErrorMessages.USER_NOT_FOUND.getMessageKey(), HttpStatus.NOT_FOUND);
    }

    public UserNotFoundException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }
}
