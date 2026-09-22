package com.vempa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "productos")
@Getter
@Setter
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false, length = 50)
    private String categoria;

    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // cascade + orphanRemoval: al guardar/borrar el producto, sus variantes
    // se guardan/borran con él (equivalente al "ON DELETE CASCADE" del SQL).
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Variante> variantes = new ArrayList<>();

    @PrePersist
    void alCrear() {
        this.createdAt = LocalDateTime.now();
    }

    public void reemplazarVariantes(List<Variante> nuevas) {
        this.variantes.clear();
        for (Variante v : nuevas) {
            v.setProducto(this);
            this.variantes.add(v);
        }
    }
}
