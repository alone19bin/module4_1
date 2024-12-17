package com.example.individuals_api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

@Getter
public class RefreshTokenException extends ErrorResponseException {

    private final String message = "Invalid or expired refresh token";

    public RefreshTokenException() {
        super(HttpStatus.UNAUTHORIZED);
    }
}
