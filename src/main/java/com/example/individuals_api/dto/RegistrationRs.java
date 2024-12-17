package com.example.individuals_api.dto;


public record RegistrationRs(
    String token,
    long expiresIn,
    String refreshToken,
    String tokenType
) {

}