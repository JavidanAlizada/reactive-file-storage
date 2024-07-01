package io.teletronics.storage_app.exception;

import org.springframework.http.HttpStatus;

public class TagNotExistException extends BaseException {

    public TagNotExistException() {
        this(ErrorMessages.TAG_INVALID_ERROR.getMessageKey(), HttpStatus.BAD_REQUEST);
    }

    public TagNotExistException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }
}
