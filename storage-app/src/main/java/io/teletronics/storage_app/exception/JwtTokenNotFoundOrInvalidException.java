package io.teletronics.storage_app.exception;

import org.springframework.http.HttpStatus;

public class JwtTokenNotFoundOrInvalidException extends BaseException {

    public JwtTokenNotFoundOrInvalidException() {
        this(ErrorMessages.JWT_TOKEN_NOT_FOUND_OR_INVALID.getMessageKey(), HttpStatus.BAD_REQUEST);
    }

    public JwtTokenNotFoundOrInvalidException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }
}