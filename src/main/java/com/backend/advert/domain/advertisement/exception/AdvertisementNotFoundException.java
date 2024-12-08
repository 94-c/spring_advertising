package com.backend.advert.domain.advertisement.exception;

import com.backend.advert.common.exception.ServiceException;

public class AdvertisementNotFoundException extends ServiceException {
    private static final String ERROR_CODE = "ADVERTISEMENT_NOT_FOUND";

    public AdvertisementNotFoundException(String message) {
        super(message, ERROR_CODE);
    }
}
