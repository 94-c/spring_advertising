package com.backend.advert.common.handler;

import com.backend.advert.common.exception.CustomErrorResponse;
import com.backend.advert.common.exception.ServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ServiceException 처리
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<CustomErrorResponse> handleServiceException(ServiceException e) {
        CustomErrorResponse errorResponse = CustomErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage(),
                "SERVICE_ERROR"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // 기타 Exception 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomErrorResponse> handleException(Exception e) {
        CustomErrorResponse errorResponse = CustomErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred.",
                "INTERNAL_SERVER_ERROR"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
