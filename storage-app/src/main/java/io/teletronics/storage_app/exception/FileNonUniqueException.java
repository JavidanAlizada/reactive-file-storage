package io.teletronics.storage_app.exception;

import org.springframework.http.HttpStatus;

import static io.teletronics.storage_app.exception.ErrorMessages.FILE_NON_UNIQUE_ERROR;

public class FileNonUniqueException extends BaseException {

    public FileNonUniqueException() {
        this(FILE_NON_UNIQUE_ERROR.getMessageKey(), HttpStatus.BAD_REQUEST);
    }

    public FileNonUniqueException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }
}
