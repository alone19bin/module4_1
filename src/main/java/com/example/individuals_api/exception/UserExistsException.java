package com.example.individuals_api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

@Getter
public class UserExistsException extends ErrorResponseException {
    private final String message = "User with this email already exists";

    public UserExistsException() {
        super(HttpStatus.CONFLICT);
    }
}
