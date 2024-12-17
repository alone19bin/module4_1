package com.example.individuals_api.exception.handler;

import com.example.individuals_api.dto.ErrorRs;
import com.example.individuals_api.exception.*;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


//обработчик исключений для  spring webflux
@ControllerAdvice
public class CustomResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({UserNotFoundException.class})
    public Mono<ResponseEntity<ErrorRs>> handleUserNotFoundException(UserNotFoundException ex, ServerWebExchange exchange) {
        return createResponseEntity(ex.getMessage(), ex.getStatusCode());
    }

    @ExceptionHandler({PersonException.class})
    public Mono<ResponseEntity<ErrorRs>> handlePersonException(PersonException ex, ServerWebExchange exchange) {
        return createResponseEntity(ex.getMsg(), ex.getStatusCode());
    }

    @ExceptionHandler({UserExistsException.class})
    public Mono<ResponseEntity<ErrorRs>> handleUserExistsException(UserExistsException ex, ServerWebExchange exchange) {
        return createResponseEntity(ex.getMessage(), ex.getStatusCode());
    }

    @ExceptionHandler({AdminException.class})
    public Mono<ResponseEntity<ErrorRs>> handleUnauthorizedException(AdminException ex, ServerWebExchange exchange) {
        return createResponseEntity(ex.getMessage(), ex.getStatusCode());
    }

    @ExceptionHandler({LoginException.class})
    public Mono<ResponseEntity<ErrorRs>> handleLoginException(LoginException ex, ServerWebExchange exchange) {
        return createResponseEntity(ex.getMessage(), ex.getStatusCode());
    }

    @ExceptionHandler({RegistrationException.class})
    public Mono<ResponseEntity<ErrorRs>> handleRegistrationException(RegistrationException ex, ServerWebExchange exchange) {
        return createResponseEntity(ex.getMessage(), ex.getStatusCode());
    }

    @ExceptionHandler({PassConfirmationDoesNotMatchException.class})
    public Mono<ResponseEntity<ErrorRs>> handlePassConfirmationException(PassConfirmationDoesNotMatchException ex, ServerWebExchange exchange) {
        return createResponseEntity(ex.getMessage(), ex.getStatusCode());
    }

    @ExceptionHandler({RefreshTokenException.class})
    public Mono<ResponseEntity<ErrorRs>> handle(RefreshTokenException ex, ServerWebExchange exchange) {
        return createResponseEntity(ex.getMessage(), ex.getStatusCode());
    }

    private Mono<ResponseEntity<ErrorRs>> createResponseEntity(String message, HttpStatusCode statusCode) {
        ErrorRs errorRs = new ErrorRs(message, statusCode.value());
        return Mono.just(ResponseEntity.status(statusCode).body(errorRs));
    }
}
