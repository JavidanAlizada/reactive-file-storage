package io.teletronics.storage_app.controller;

import io.teletronics.storage_app.exception.BaseException;
import io.teletronics.storage_app.exception.ErrorMessages;
import io.teletronics.storage_app.exception.ErrorResponse;
import io.teletronics.storage_app.util.MessageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Date;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final MessageUtil messageUtil;

    @Autowired
    public GlobalExceptionHandler(MessageUtil messageUtil) {
        this.messageUtil = messageUtil;
    }

    @ExceptionHandler(BaseException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleBaseException(BaseException ex, ServerWebExchange webExchange) {
        String errorMessage = messageUtil.getMessage(ErrorMessages.fromValue(ex.getMessage()));
        ErrorResponse errorResponse = new ErrorResponse(errorMessage, ex.getHttpStatus(), ex.getDate());
        return Mono.just(new ResponseEntity<>(errorResponse, ex.getHttpStatus()));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGeneralException(Exception ex, ServerWebExchange webExchange) {
        String errorMessage = messageUtil.getMessage(ErrorMessages.INTERNAL_ERROR);
        ErrorResponse errorResponse = new ErrorResponse(errorMessage + " : " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, new Date());
        return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(DataBufferLimitException.class)
    public ResponseEntity<ErrorResponse> handleMaxSizeException(DataBufferLimitException exc) {
        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ErrorResponse("File is too large! : " + exc.getMessage(), HttpStatus.BAD_REQUEST, new Date()));
    }
}
