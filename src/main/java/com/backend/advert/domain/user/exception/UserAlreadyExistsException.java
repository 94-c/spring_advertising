package com.backend.advert.domain.user.exception;

import com.backend.advert.common.exception.ServiceException;

public class UserAlreadyExistsException extends ServiceException {
    private static final String ERROR_CODE = "USER_ALREADY_EXISTS";

    public UserAlreadyExistsException(String message) {
        super(message, ERROR_CODE);
    }
}
