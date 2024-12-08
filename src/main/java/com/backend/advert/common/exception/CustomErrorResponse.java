package com.backend.advert.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CustomErrorResponse {
    private final int status;
    private final String message;
    private final String errorCode;
    private final String timestamp;

    public CustomErrorResponse(int status, String message, String errorCode) {
        this.status = status;
        this.message = message;
        this.errorCode = errorCode;
        this.timestamp = java.time.LocalDateTime.now().toString();
    }

    public static CustomErrorResponse of(int status, String message, String errorCode) {
        return new CustomErrorResponse(status, message, errorCode);
    }
}
