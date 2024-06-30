package io.teletronics.storage_app.exception;

import org.springframework.http.HttpStatus;

import static io.teletronics.storage_app.exception.ErrorMessages.TAG_SIZE_EXCEED_LIMIT_ERROR;


public class TagSizeExceedLimitException extends BaseException {

    public TagSizeExceedLimitException() {
        this(TAG_SIZE_EXCEED_LIMIT_ERROR.getMessageKey(), HttpStatus.BAD_REQUEST);
    }

    public TagSizeExceedLimitException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }
}
