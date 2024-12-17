package com.example.individuals_api.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;


//для взаимодействия с микросервисами

@Configuration
public class WebClientConfig {
            //URL  person-ms  из конфигурации
    @Value("${person-ms.url}")
    private String personMsUrl;



        // WebClient для взаимодействия с person-ms
    @Bean
    public WebClient personMsWebClient() {
            //создание WebClient с  базовым URL из конфига
        return WebClient.builder()
            .baseUrl(personMsUrl)
            .build();
    }
}
