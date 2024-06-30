package io.teletronics.storage_app.exception;

import org.springframework.http.HttpStatus;

import static io.teletronics.storage_app.exception.ErrorMessages.FILE_SAVING_ERROR;

public class FileSavingException extends BaseException {

    public FileSavingException() {
        this(FILE_SAVING_ERROR.getMessageKey(), HttpStatus.BAD_REQUEST);
    }

    public FileSavingException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }
}
