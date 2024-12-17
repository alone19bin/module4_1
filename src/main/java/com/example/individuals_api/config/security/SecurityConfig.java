package com.example.individuals_api.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.web.reactive.function.client.ServerBearerExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@RequiredArgsConstructor
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    // Конвертер JWT токенов для преобразования claims в authorities
    private final JwtConverter jwtConverter;


    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) throws Exception {
        http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                 //настройка  правил авторизации для эндпоинтов
            .authorizeExchange(customizer -> {
                customizer
                    //эндпоинт для регистрации
                    .pathMatchers(HttpMethod.POST, "/v1/auth/registration/**").permitAll()
                        // эндпоинт для входа
                    .pathMatchers(HttpMethod.POST, "/v1/auth/login/**").permitAll()
                    //эндпоинты обновления токена
                    .pathMatchers(HttpMethod.POST, "/v1/auth/refresh-token/**").permitAll();
                                 // Все     остальные эндпоинты
                customizer.anyExchange().authenticated();
            });

        //OAuth2 acsess с JWT
        http
            .oauth2ResourceServer(customizer -> customizer
                .jwt(jwtCustomizer -> {
                    //Используем свой конвертер для преобрзования jwt
                    jwtCustomizer.jwtAuthenticationConverter(jwtConverter);
                }))
            //использование NoOp репозитория для  безопасности
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance());



                //построение и   возврат  цепочки фильтров
        return http.build();
    }
}