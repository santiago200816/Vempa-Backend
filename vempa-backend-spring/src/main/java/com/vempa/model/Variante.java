package com.vempa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "variantes")
@Getter
@Setter
public class Variante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    @JsonIgnore // evita recursión infinita al serializar Producto <-> Variante
    private Producto producto;

    @Column(nullable = false, length = 100)
    private String color;

    @Column(length = 50)
    private String talla;

    @Column(nullable = false)
    private int stock = 0;
}
