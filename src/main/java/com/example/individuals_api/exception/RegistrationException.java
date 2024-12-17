package com.example.individuals_api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

@Getter
public class RegistrationException extends ErrorResponseException {
    private final String message = "Invalid email or password";

    public RegistrationException() {
        super(HttpStatus.UNAUTHORIZED);
    }

    public RegistrationException(Throwable cause) {
        super(HttpStatus.UNAUTHORIZED, cause);
    }
}
