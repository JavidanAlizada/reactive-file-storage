package io.teletronics.storage_app.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends BaseException {

    public InvalidCredentialsException() {
        this(ErrorMessages.INVALID_CREDENTIALS_ERROR.getMessageKey(), HttpStatus.BAD_REQUEST);
    }

    public InvalidCredentialsException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }
}
