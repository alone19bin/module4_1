package com.example.individuals_api;

import com.example.individuals_api.dto.*;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import static org.mockito.Mockito.when;

import java.time.Duration;


@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IndividualsApiApplicationTests extends ABaseTest {
       //Случайный порт для запуска тестового сервера&
    @LocalServerPort
    private int randomServerPort;

    private WebTestClient webClient;


      //Настройка  перед каждым тест,  базовый URL и время ожидания ответа
    @BeforeEach
    void setup() {
        this.webClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + randomServerPort)
                .responseTimeout(Duration.ofMillis(30000))
                .build();
    }


        //Остановка keycloak после завершения тестов

    @PreDestroy
    void stopContainer() {
        if (keycloak != null) {
            keycloak.stop();
        }
    }

       // попытку получения данных пользователя без токена автризаци

    @Test
    void meWithoutAuthToken() {
        webClient
                .get()
                .uri("/v1/auth/me")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isEqualTo(HttpStatus.UNAUTHORIZED)
                .expectBody();
    }


     //регистрация нового пользователя

    @Test
    void register() {
        RegistrationRq rq = new RegistrationRq(
            "unique_" + System.currentTimeMillis() + "@ya.ru", 
            "qwe123", 
            "qwe123", 
            null, 
            null
        );
        webClient
            .post()
            .uri("/v1/auth/registration")
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(rq))
            .exchange()
            .expectStatus().isOk()
            .expectBody(RegistrationRs.class)
            .consumeWith(response -> {
                log.info("Registration Response: {}", response.getResponseBody());
            })
            .value(registrationRs -> {
                Assertions.assertNotNull(registrationRs);
                Assertions.assertNotNull(registrationRs.token());
            });
    }

      // регистрациюя нового пользователя с неверным  паролем

    @Test
    void registerPwdDoesNotMatch() {
        RegistrationRq rq = new RegistrationRq("alina@ya.ru", "qwe123", "qwe12345",
                null, null);
        webClient
            .post()
            .uri("/v1/auth/registration")
            .bodyValue(rq)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.BAD_REQUEST)
            .expectBody(ErrorRs.class)
            .value(Assertions::assertNotNull);
    }


     // регистрациюя нового пользователя сс уже существующим логином.

    @Test
    void registerUserExists() {
        RegistrationRq rq = new RegistrationRq("user@user.com", "user", "user",
                null, null);
        webClient
            .post()
            .uri("/v1/auth/registration")
            .bodyValue(rq)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isEqualTo(HttpStatus.CONFLICT)
            .expectBody(ErrorRs.class)
            .value(Assertions::assertNotNull);
    }


      //авторизацию существующего пользователя
    @Test
    void login() {
        LoginRq rq = new LoginRq("user@user.com", "user");
        RegistrationRs responseBody = webClient
            .post()
            .uri("/v1/auth/login")
            .bodyValue(rq)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(RegistrationRs.class)
            .returnResult()
            .getResponseBody();
        Assertions.assertNotNull(responseBody);
    }


     //аутентификация: логин и получение данных  пльзователя

    @Test
    void loginAndMe() {
        LoginRq loginRq = new LoginRq("user@user.com", "user");
        RegistrationRs loginRs = webClient
            .post()
            .uri("/v1/auth/login")
            .bodyValue(loginRq)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(RegistrationRs.class)
            .returnResult()
            .getResponseBody();

        Assertions.assertNotNull(loginRs);
        webClient
            .get()
            .uri("/v1/auth/me")
            .accept(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + loginRs.token())
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody();
    }


        //обновление токена авторизации

    @Test
    void loginAndRefresh() {
        LoginRq loginRq = new LoginRq("user@user.com", "user");
        RegistrationRs loginRs = webClient
            .post()
            .uri("/v1/auth/login")
            .bodyValue(loginRq)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(RegistrationRs.class)
            .returnResult()
            .getResponseBody();
        Assertions.assertNotNull(loginRs);
        webClient
            .post()
            .uri("/v1/auth/refresh-token")
            .accept(MediaType.APPLICATION_JSON)
            .bodyValue(new RefreshTokenRq(loginRs.refreshToken()))
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody();
    }
}
