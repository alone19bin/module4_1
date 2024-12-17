package com.example.individuals_api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

@Getter
public class PassConfirmationDoesNotMatchException extends ErrorResponseException {
    private final String message = "Password confirmation does not match";

    public PassConfirmationDoesNotMatchException() {
        super(HttpStatus.BAD_REQUEST);
    }
}
