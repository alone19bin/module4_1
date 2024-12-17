package com.example.individuals_api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

@Getter
public class LoginException extends ErrorResponseException {
    private final String message = "Invalid email or password";

    public LoginException() {
        super(HttpStatus.UNAUTHORIZED);
    }
}
