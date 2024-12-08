package com.backend.advert.domain.advertisement.exception;

import com.backend.advert.common.exception.ServiceException;

public class AdvertisementAlreadyExistsException extends ServiceException {
    private static final String ERROR_CODE = "ADVERTISEMENT_ALREADY_EXISTS";

    public AdvertisementAlreadyExistsException(String message) {
        super(message, ERROR_CODE);
    }
}
