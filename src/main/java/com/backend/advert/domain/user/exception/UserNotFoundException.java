package com.backend.advert.domain.user.exception;

import com.backend.advert.common.exception.ServiceException;

public class UserNotFoundException extends ServiceException {
    private static final String ERROR_CODE = "USER_NOT_FOUND";

    public UserNotFoundException(String message) {
        super(message, ERROR_CODE);
    }
}
