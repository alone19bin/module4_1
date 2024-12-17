package com.example.individuals_api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

@Getter
public class AdminException extends ErrorResponseException {
    private final String message = "Invalid admin";

    public AdminException() {
        super(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public AdminException(Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, cause);
    }
}
