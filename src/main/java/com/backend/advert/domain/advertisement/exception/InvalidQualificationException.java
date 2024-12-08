package com.backend.advert.domain.advertisement.exception;

import com.backend.advert.common.exception.ServiceException;

public class InvalidQualificationException extends ServiceException {
    private static final String ERROR_CODE = "INVALID_QUALIFICATION";

    public InvalidQualificationException(String message) {
        super(message, ERROR_CODE);
    }
}
