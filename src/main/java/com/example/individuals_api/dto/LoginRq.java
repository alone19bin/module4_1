package com.example.individuals_api.dto;


public record LoginRq(
    String email,
    String password
) {}