package com.vempa.dto;

import com.vempa.model.Producto;
import com.vempa.model.Variante;

import java.math.BigDecimal;
import java.util.List;

public record ProductoResponse(
    Long id,
    String nombre,
    String descripcion,
    BigDecimal precio,
    String categoria,
    String imagenUrl,
    boolean activo,
    List<VarianteDTO> variantes
) {
    public static ProductoResponse desde(Producto p) {
        List<VarianteDTO> variantes = p.getVariantes().stream()
            .map(v -> new VarianteDTO(v.getId(), v.getColor(), v.getTalla(), v.getStock()))
            .toList();

        return new ProductoResponse(
            p.getId(), p.getNombre(), p.getDescripcion(), p.getPrecio(),
            p.getCategoria(), p.getImagenUrl(), p.isActivo(), variantes
        );
    }
}
