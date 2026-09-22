package com.vempa.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroClienteRequest(
    @NotBlank String nombre,
    @NotBlank @Email String email,
    String telefono,
    @NotBlank @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres") String password
) {}
