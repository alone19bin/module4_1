package com.example.individuals_api.service;

import com.example.individuals_api.exception.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

//управления токенами и аутентификацией через Keycloak
@Service
@Slf4j
@RequiredArgsConstructor
public class TokenService {
     //Конфигурационные параметры Keycloak
    @Value("${keycloak.url}")
    private String keycloakUrl;
    @Value("${keycloak.realm}")
    private String realm;
    @Getter
    @Value("${jwt.auth.converter.resource-id}")
    private String clientId;
    @Value("${keycloak.secret}")
    private String secret;

       //Регистрация нового пользователя с получением токенов
    public Mono<AccessTokenResponse> register(UserRepresentation rep) {
        //получение admin-токена для регистрации
        Mono<AccessTokenResponse> admin = getAdminToken();

        //создание пользователя  через admin API keycloak
        Mono<ResponseEntity<Void>> responseEntityMono = admin.flatMap(accessTokenResponse ->
                getAdminInstance()
                        .post()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(rep))
                        .headers(h -> h.setBearerAuth(accessTokenResponse.getToken()))
                        .retrieve()
                        .toBodilessEntity());

                //Получение токенов для нового пользователя
        return responseEntityMono.flatMap(rs ->
                getToken(rep.getUsername(), rep.getCredentials().getFirst().getValue()));
    }

      //gодсчет количества пользователей с  указанным email
    public Mono<Long> findUserCount(String email) {
        // Получение admin-токена для запроса
        Mono<AccessTokenResponse> admin = getAdminToken();
        return admin.flatMap(accessTokenResponse ->
                getAdminInstance()
                        .get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/count")
                                .queryParam("email", email)
                                .build())
                        .headers(h -> h.setBearerAuth(accessTokenResponse.getToken()))
                        .retrieve()
                        .bodyToMono(Long.class)
                        .onErrorMap(RegistrationException::new));
    }

        //gолучение токенов для пользователя по логину и паролю
    public Mono<AccessTokenResponse> getToken(String username, String password) {
        // Подготовка параметров для OAuth2 запроса
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", secret);
        params.add("username", username);
        params.add("password", password);
        params.add("grant_type", OAuth2Constants.PASSWORD);
        params.add("scope", "openid");

        // Выполнение запроса на получение токенов
        return getInstance("/protocol/openid-connect/token")
                .post()
                .body(BodyInserters.fromFormData(params))
                .retrieve()
                .bodyToMono(AccessTokenResponse.class)
                .onErrorMap(e -> new LoginException());
    }

    //gолучение информации о пользователе по его id
    public Mono<UserRepresentation> getUserRepresentation(String userId) {
        // Получение admin-токена для запроса
        Mono<AccessTokenResponse> admin = getAdminToken();
        return admin.flatMap(accessTokenResponse ->
                getAdminInstance()
                        .get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/" + userId)
                                .build())
                        .headers(h -> h.setBearerAuth(accessTokenResponse.getToken()))
                        .retrieve()
                        .bodyToMono(UserRepresentation.class)
                        .onErrorMap(e -> {
                            log.info(e.getMessage());
                            throw new UserNotFoundException();
                        }));
    }

             //Обновление токена с использованием refresh токена
    public Mono<AccessTokenResponse> getRefreshToken(String token) {
        // Подготовка параметров для обновления токена
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", secret);
        params.add("refresh_token", token);
        params.add("grant_type", OAuth2Constants.REFRESH_TOKEN);

             // Выполнение запроса на получение новых токенов
        return getInstance("/protocol/openid-connect/token").post()
                .body(BodyInserters.fromFormData(params))
                .retrieve()
                .bodyToMono(AccessTokenResponse.class)
                .onErrorMap(e -> new RefreshTokenException());
    }

    //cоздание WebClient для admin API keycloa
    private WebClient getAdminInstance() {
        String url = keycloakUrl + "/admin/realms/" + realm + "/users";
        return WebClient.builder()
                .baseUrl(url)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
    }

        // получение adminтокена для служебных операций
    private Mono<AccessTokenResponse> getAdminToken() {
        // Подготовка параметров для получения admin-токена
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", secret);
        params.add("scope", "openid");
        params.add("grant_type", OAuth2Constants.CLIENT_CREDENTIALS);
        
           //запрос на получение admin-токена
        return getInstance("/protocol/openid-connect/token")
                .post()
                .body(BodyInserters.fromFormData(params))
                .retrieve()
                .bodyToMono(AccessTokenResponse.class)
                .onErrorMap(AdminException::new);
    }

            //создание базового WebClient для взаимодействия с Keycloak
    private WebClient getInstance(String uri) {
        String url = keycloakUrl + "/realms/" + realm + uri;
        return WebClient.builder()
                .baseUrl(url)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
    }
}
