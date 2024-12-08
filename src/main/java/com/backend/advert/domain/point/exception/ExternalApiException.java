package com.backend.advert.domain.point.exception;

import com.backend.advert.common.exception.ServiceException;

public class ExternalApiException extends ServiceException {
    private static final String ERROR_CODE = "EXTERNAL_API_ERROR";

    public ExternalApiException(String message) {
        super(message, ERROR_CODE);
    }
}
