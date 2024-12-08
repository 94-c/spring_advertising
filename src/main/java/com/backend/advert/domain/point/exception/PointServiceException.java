package com.backend.advert.domain.point.exception;

import com.backend.advert.common.exception.ServiceException;

public class PointServiceException extends ServiceException {
    private static final String ERROR_CODE = "POINT_SERVICE_ERROR";

    public PointServiceException(String message) {
        super(message, ERROR_CODE);
    }
}
