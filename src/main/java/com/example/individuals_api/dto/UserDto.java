package com.example.individuals_api.dto;

import java.time.LocalDateTime;
import java.util.Set;


public record UserDto(
    String id, 
    String email, 
    Set<String> roles, 
    LocalDateTime createdAt,
    String firstName,
    String lastName
) {}