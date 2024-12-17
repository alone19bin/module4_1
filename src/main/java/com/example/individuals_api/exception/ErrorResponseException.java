package com.example.individuals_api.exception;
import org.springframework.http.HttpStatus;
import java.net.ConnectException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public class ErrorResponseException extends RuntimeException {
    private final HttpStatus statusCode;

    public ErrorResponseException(String message, HttpStatus statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public ErrorResponseException(String message, Throwable cause, HttpStatus statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }
}