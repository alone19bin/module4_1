package com.example.individuals_api;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.keycloak.OAuth2Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;

public abstract class ABaseTest {
    private static final String KEYCLOAK_IMAGE = "quay.io/keycloak/keycloak:26.0.1";
    private static final String REALM_EXPORT_JSON = "/realm-export.json";
    private static final Logger log = LoggerFactory.getLogger(ABaseTest.class);
        //базовый класс для тестов с настройкой keycloak
    @Container
    public static KeycloakContainer keycloak;
        //Инициализация keycloak с импортом realm из JSON
    static {
        keycloak = new KeycloakContainer(KEYCLOAK_IMAGE)
                .withRealmImportFile(REALM_EXPORT_JSON);
        keycloak.start();// Автоматический запуск контейнера
    }
            //динамическая настройка свойств для подключения к Keycloak?
                //автоматическое определение URL и параметров безопасности
    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        log.info("keycloak.getFirstMappedPort()={}", keycloak.getFirstMappedPort());
        registry.add("keycloak.url",
                () -> "http://localhost:" + keycloak.getFirstMappedPort());
        registry.add("keycloak.auth-server-url",
                () -> "http://localhost:" + keycloak.getFirstMappedPort());
        registry.add("keycloak.get-token-url",
                () -> "http://localhost:" + keycloak.getFirstMappedPort() + "/realms/myrealm/protocol/openid-connect/token");
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> "http://localhost:" + keycloak.getFirstMappedPort() + "/realms/myrealm");
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
                () -> "http://localhost:" + keycloak.getFirstMappedPort() + "/realms/myrealm/protocol/openid-connect/certs");
        registry.add("keycloak.realm", () -> "myrealm");
        registry.add("keycloak.grant-type", () -> OAuth2Constants.CLIENT_CREDENTIALS);
        registry.add("keycloak.client-id", () -> "individuals-api");
        registry.add("keycloak.client-secret", () -> "U2VuDnofJOfL9K0Dct1X3NjUlVIYCkHa");
        registry.add("scope", () -> "openid");
    }
}
