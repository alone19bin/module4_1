package com.example.individuals_api.exception;

import com.example.individuals_api.dto.ErrorRs;
import com.example.individuals_api.exception.ErrorResponseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;
import com.example.individuals_api.exception.PassConfirmationDoesNotMatchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@RestControllerAdvice
public class GlobalExceptionHandler {
            //логгер  для записи информации об ошибках
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    @ExceptionHandler(ErrorResponseException.class)
    public Mono<ResponseEntity<ErrorRs>> handleErrorResponseException(ErrorResponseException ex) {
        log.error("Error response exception: {}", ex.getMessage(), ex);
        return Mono.just(ResponseEntity
            .status(HttpStatus.valueOf(ex.getStatusCode().value()))
            .body(new ErrorRs(
                ex.getMessage(), 
                ex.getMessage(),
                ex.getStatusCode().value()
            )));
    }


    @ExceptionHandler(RuntimeException.class)
    public Mono<ResponseEntity<ErrorRs>> handleRuntimeException(RuntimeException ex) {
        // Проверяем корневую причину
        Throwable rootCause = ex;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }

        // Если корневая причина - ErrorResponseException
        if (rootCause instanceof ErrorResponseException errorEx) {
            log.error("Runtime error with ErrorResponseException", ex);
            return Mono.just(ResponseEntity
                .status(HttpStatus.valueOf(errorEx.getStatusCode().value()))
                .body(new ErrorRs(
                    errorEx.getMessage(), 
                    errorEx.getMessage(),
                    errorEx.getStatusCode().value()
                )));
        }

        // Неизвестные runtime исключения
        log.error("Unexpected runtime error", ex);
        return Mono.just(ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorRs(
                "Unexpected error", 
                ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
            )));
    }

     //Универсальный обработчик для всех прочих исключений.

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorRs>> handleGeneralException(Exception ex) {
        log.error("Unexpected error during request processing", ex);
        
        // Check for nested ErrorResponseException
        Throwable rootCause = ex;
        while (rootCause.getCause() != null) {
            if (rootCause.getCause() instanceof ErrorResponseException errorEx) {
                return Mono.just(ResponseEntity
                    .status(HttpStatus.valueOf(errorEx.getStatusCode().value()))
                    .body(new ErrorRs(
                        errorEx.getMessage(), 
                        errorEx.getMessage(),
                        errorEx.getStatusCode().value()
                    )));
            }
            rootCause = rootCause.getCause();
        }
        
        // Default error handling
        return Mono.just(ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorRs(
                "Unexpected error", 
                ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
            )));
    }


    @ExceptionHandler(PassConfirmationDoesNotMatchException.class)
    public Mono<ResponseEntity<ErrorRs>> handlePasswordConfirmationException(PassConfirmationDoesNotMatchException ex) {
            //несоответствия паролей
        log.error("Password confirmation error", ex);
        return Mono.just(ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorRs(
                "Password confirmation does not match", 
                "Passwords do not match",
                HttpStatus.BAD_REQUEST.value()
            )));
    }


    @ExceptionHandler(WebClientResponseException.class)
    public Mono<ResponseEntity<ErrorRs>> handleWebClientException(WebClientResponseException ex) {
        // полное тело  ответа и детали ошибки  для WebClient
        log.error("WebClient error: {}", ex.getResponseBodyAsString(), ex);

        return Mono.just(ResponseEntity
            .status(HttpStatus.valueOf(ex.getStatusCode().value()))
            .body(new ErrorRs(
                "WebClient error", 
                ex.getMessage(),
                ex.getStatusCode().value()
            )));
    }
}