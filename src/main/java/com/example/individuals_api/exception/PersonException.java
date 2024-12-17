package com.example.individuals_api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

@Getter
public class PersonException extends ErrorResponseException {
    private final String msg;

    public PersonException(String msg) {
        super(HttpStatus.NOT_ACCEPTABLE);
        this.msg = msg;
    }
}
