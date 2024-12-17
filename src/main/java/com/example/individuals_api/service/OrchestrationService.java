package com.example.individuals_api.service;

import com.example.individuals_api.dto.RegistrationRq;
import com.example.individuals_api.dto.RegistrationRs;
import com.example.individuals_api.exception.AdminException;
import com.example.individuals_api.exception.ErrorResponseException;
import com.example.individuals_api.exception.LoginException;
import com.example.individuals_api.exception.PersonException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpStatus;
import java.net.ConnectException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import java.net.ConnectException;
import java.util.UUID;


 //создание персональной записи и регистрации пользователя.
@Service
@RequiredArgsConstructor
@Slf4j
public class OrchestrationService {

    private final UserService userService;
    
    // WebClient для person-ms
    private final WebClient personMsWebClient;


    public Mono<RegistrationRs> register(RegistrationRq rq) {
               //POST запрос в person-ms для создания персональной записи
        return personMsWebClient.post()
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(rq))
            .retrieve()
            .bodyToMono(UUID.class)
            .onErrorResume(ex -> {
                         //ошибока подключения с логированием
                log.error("Error connecting to person-ms: {}", ex.getMessage());
                    //случайный UUID для тестирования?
                return Mono.just(UUID.randomUUID());
            })
            .flatMap(uuid -> {
                   // логирование полученного UUID
                log.info("Received UUID from person-ms: {}", uuid);
                //Регистрация пользователя с полученным UUID
                return userService.register(rq, uuid);
            });
    }


}
