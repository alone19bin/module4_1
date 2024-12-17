package com.example.individuals_api.dto;


public record RegistrationRq(
    String email,
    String password,
    String confirmPassword,
    String firstName,  
    String lastName 
) {}