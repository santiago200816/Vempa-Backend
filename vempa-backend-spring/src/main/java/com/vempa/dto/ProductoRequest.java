package com.vempa.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record ProductoRequest(
    @NotBlank String nombre,
    String descripcion,
    @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal precio,
    @NotBlank String categoria,
    String imagenUrl,
    Boolean activo,
    @Valid List<VarianteDTO> variantes
) {}
