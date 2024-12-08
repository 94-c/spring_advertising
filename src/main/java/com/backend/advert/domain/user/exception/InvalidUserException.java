package com.backend.advert.domain.user.exception;

import com.backend.advert.common.exception.ServiceException;

public class InvalidUserException extends ServiceException {
    private static final String ERROR_CODE = "INVALID_USER";

    public InvalidUserException(String message) {
        super(message, ERROR_CODE);
    }
}
