package io.teletronics.storage_app.exception;

import org.springframework.http.HttpStatus;

import static io.teletronics.storage_app.exception.ErrorMessages.INVALID_VISIBILITY_VALUE;

public class InvalidVisibilityValueException extends BaseException {

    public InvalidVisibilityValueException() {
        this(INVALID_VISIBILITY_VALUE.getMessageKey(), HttpStatus.BAD_REQUEST);
    }

    public InvalidVisibilityValueException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }
}
