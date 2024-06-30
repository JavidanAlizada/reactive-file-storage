package io.teletronics.storage_app.exception;

import org.springframework.http.HttpStatus;

import static io.teletronics.storage_app.exception.ErrorMessages.FILE_UPLOAD_ERROR;
import static io.teletronics.storage_app.exception.ErrorMessages.USER_EXIST;

public class FileUploadException extends BaseException {

    public FileUploadException() {
        this(FILE_UPLOAD_ERROR.getMessageKey(), HttpStatus.BAD_REQUEST);
    }

    public FileUploadException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }
}
