package com.example.individuals_api.service;

import com.example.individuals_api.dto.LoginRq;
import com.example.individuals_api.dto.RegistrationRq;
import com.example.individuals_api.dto.RegistrationRs;
import com.example.individuals_api.dto.UserDto;
import com.example.individuals_api.exception.PassConfirmationDoesNotMatchException;
import com.example.individuals_api.exception.UserExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;
import org.springframework.http.HttpStatus;
import com.example.individuals_api.exception.ErrorResponseException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.individuals_api.enums.Role.USER;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final TokenService tokenService;

    public Mono<Boolean> validateRegistration(@NonNull RegistrationRq rq) {
        if (!isPwdConfirmed(rq.password(), rq.confirmPassword())) {
            return Mono.error(new ErrorResponseException(
                "Password confirmation does not match", 
                HttpStatus.BAD_REQUEST
            ));
        }
        return isNew(rq.email())
                .flatMap(count -> {
                    if (count > 0) {
                        return Mono.error(new ErrorResponseException(
                            "User with this email already exists", 
                            HttpStatus.CONFLICT
                        ));
                    }
                    return Mono.just(true);
                });
    }


    public Mono<RegistrationRs> register(RegistrationRq rq, UUID personId) {
        // Проверка совпадения паролей
        if (!rq.password().equals(rq.confirmPassword())) {
            return Mono.error(new ErrorResponseException(
                "Password confirmation does not match", 
                HttpStatus.BAD_REQUEST
            ));
        }

        // проверка существования пользователя
        return isNew(rq.email())
            .flatMap(count -> {
                if (count > 0) {
                    return Mono.error(new ErrorResponseException(
                        "User with this email already exists", 
                        HttpStatus.CONFLICT
                    ));
                }
                
                UserRepresentation userRepresentation = create(rq, personId);
                log.info("Creating user representation: {}", userRepresentation);
                
                return tokenService.register(userRepresentation)
                    .map(accessToken -> {
                        log.info("Registration successful for user: {}", rq.email());
                        return new RegistrationRs(
                            accessToken.getToken(), 
                            accessToken.getExpiresIn(),
                            accessToken.getRefreshToken(),
                            accessToken.getTokenType()
                        );
                    })
                    .onErrorResume(ex -> {
                        log.error("Token registration error", ex);
                        return Mono.error(new ErrorResponseException(
                            "Token registration failed", 
                            HttpStatus.INTERNAL_SERVER_ERROR
                        ));
                    });
            });
    }


        //Аутентификация пользователя
    public Mono<RegistrationRs> login(LoginRq rq) {
        Mono<AccessTokenResponse> byUser = tokenService.getToken(rq.email(), rq.password());
        return byUser.map(accessToken -> new RegistrationRs(accessToken.getToken(),
                accessToken.getExpiresIn(), accessToken.getRefreshToken(),
                accessToken.getTokenType()));
    }

    public Mono<RegistrationRs> refreshToken(String token) {
        Mono<AccessTokenResponse> refreshToken = tokenService.getRefreshToken(token);
        return refreshToken.map(accessToken -> new RegistrationRs(accessToken.getToken(),
                accessToken.getExpiresIn(), accessToken.getRefreshToken(),
                accessToken.getTokenType()));
    }

    public Mono<UserDto> me(String token, String userId, Collection<GrantedAuthority> authorities) {
        Mono<UserRepresentation> userRepresentation = tokenService.getUserRepresentation(userId);
        Set<String> roles = authorities.stream().map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(s -> s.substring(5))
                .collect(Collectors.toUnmodifiableSet());
        return userRepresentation.map(rep -> {
            Long createdTimestamp = rep.getCreatedTimestamp();
            LocalDateTime createdAt =
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(createdTimestamp),
                            TimeZone.getDefault().toZoneId());
            return new UserDto(rep.getId(), rep.getEmail(), roles, createdAt, null, null);
        });
    }


             //     Проверка уникальности email-адреса пользователя
    private Mono<Long> isNew(String email) {
        return tokenService.findUserCount(email);
    }


      //роверка совпадения пароля и подтверждения пароля.
    private boolean isPwdConfirmed(String password, String confirmedPassword) {
        return password.equals(confirmedPassword);
    }


          //Создание представления пользователя для keycloak?
    private UserRepresentation create(RegistrationRq rq, UUID personId) {
        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setEmail(rq.email());
        user.setClientRoles(Map.of(tokenService.getClientId(), List.of(USER.name())));
        user.setCredentials(List.of(create(rq.password())));
        user.setUsername(rq.email());

        user.setAttributes(Map.of("person-id", List.of(personId.toString())));
        return user;
    }


       //Создание представления учетных данных для keycloak
    private CredentialRepresentation create(String pwd) {
        CredentialRepresentation pass = new CredentialRepresentation();
        pass.setTemporary(false);
        pass.setType(CredentialRepresentation.PASSWORD);
        pass.setValue(pwd);
        return pass;
    }
}
