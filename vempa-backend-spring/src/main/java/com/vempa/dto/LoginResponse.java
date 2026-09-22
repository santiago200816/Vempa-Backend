package com.vempa.dto;

public record LoginResponse(
    String token,
    String email,
    long expiraEnSegundos
) {}
