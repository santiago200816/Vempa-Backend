package com.vempa.dto;

public record ClienteAuthResponse(
    String token,
    String email,
    String nombre,
    String telefono,
    long expiraEnSegundos
) {}
