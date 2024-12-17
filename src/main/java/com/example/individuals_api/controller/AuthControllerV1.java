package com.example.individuals_api.controller;

import com.example.individuals_api.dto.LoginRq;
import com.example.individuals_api.dto.RefreshTokenRq;
import com.example.individuals_api.dto.RegistrationRq;
import com.example.individuals_api.dto.RegistrationRs;
import com.example.individuals_api.dto.UserDto;
import com.example.individuals_api.exception.ErrorResponseException;
import com.example.individuals_api.service.OrchestrationService;
import com.example.individuals_api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

//обработка запросов аутентификации и регистрации пользователей.
@Slf4j
@RestController
@RequestMapping(value = "/v1/auth", consumes = "application/json", produces = "application/json")
@RequiredArgsConstructor
public class AuthControllerV1 {   

    private final UserService userService;
    
    // для более сложной регистрации?
    private final OrchestrationService orchestrationService;


    @PostMapping(value = "/registration")
    @ResponseBody     
    public Mono<RegistrationRs> registration(@RequestBody @Valid RegistrationRq rq) {
        return userService.validateRegistration(rq)
            .flatMap(valid -> orchestrationService.register(rq))
            .onErrorResume(ex -> {
                if (ex instanceof ErrorResponseException errorEx) {
                    return Mono.error(errorEx);
                }
                return Mono.error(new ErrorResponseException(
                    "Unexpected registration error", 
                    HttpStatus.INTERNAL_SERVER_ERROR
                ));
            });
    }


    @PostMapping(value = "/login")    
    @ResponseBody
    public Mono<RegistrationRs> login(@RequestBody LoginRq rq) {
             // Выполнение входа и получение токенов
        return userService.login(rq);
    }


    @GetMapping(value = "/me", consumes = "*/*", produces = "application/json")
    public Mono<UserDto> me(@AuthenticationPrincipal JwtAuthenticationToken token) {
          //олучение информации о пользователе по токену
        return userService.me(token.getToken().getTokenValue(),
                token.getName(),
                token.getAuthorities());
    }


    @PostMapping(value = "/refresh-token")
    public Mono<RegistrationRs> refreshToken(@RequestBody RefreshTokenRq rq) {
        //Обновление токена через сервис пользователей
        return userService.refreshToken(rq.refreshToken());
    }
}
