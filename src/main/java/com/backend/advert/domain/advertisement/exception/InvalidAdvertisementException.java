package com.backend.advert.domain.advertisement.exception;

import com.backend.advert.common.exception.ServiceException;

public class InvalidAdvertisementException extends ServiceException {
    private static final String ERROR_CODE = "INVALID_ADVERTISEMENT";

    public InvalidAdvertisementException(String message) {
        super(message, ERROR_CODE);
    }
}
