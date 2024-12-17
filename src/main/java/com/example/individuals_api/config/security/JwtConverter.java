package com.example.individuals_api.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


 //конвертер JWTтокена в объект аутентификации, нужен для reactive-безопасности в spring Sec

@Component
public class JwtConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {
    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

            //идентификатор ресурса считываемый из конфига
    @Value("${jwt.auth.converter.resource-id}")
    private String resourceId;


            //преобразование JWT-токена в объект аутентификации.
    @Override
    public Mono<AbstractAuthenticationToken> convert(@NonNull Jwt source) {
        // Объединение прав доступа из стандартного конвертера и кастомного метода
        Collection<GrantedAuthority> authorities = Stream.concat(
                jwtGrantedAuthoritiesConverter.convert(source).stream(),
                extractResourceRoles(source).stream()).collect(Collectors.toSet());
        
                    //создание токена аутентфикации с правами доступа
        return Mono.just(new JwtAuthenticationToken(source, authorities, getPrincipalClaimName(source)));
    }


    private String getPrincipalClaimName(Jwt jwt) {
        // Использование стандартного claim' для идентификации
        String claimName = JwtClaimNames.SUB;
        return jwt.getClaim(claimName);
    }


    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {
            //Извлечение claims с информацией о доступе к ресурсам
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        Map<String, Object> resource;
        Collection<String> resourceRoles;

            //прверка наличия необходимых claims
        if (resourceAccess == null
                || (resource = (Map<String, Object>) resourceAccess.get(resourceId)) == null
                || (resourceRoles = (Collection<String>) resource.get("roles")) == null) {
            //Возврат пустого набора, если claims не найдены
            return Set.of();
        }
            //Преобразование ролей ресурса в права доступа spring Sec
        return resourceRoles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());
    }
}