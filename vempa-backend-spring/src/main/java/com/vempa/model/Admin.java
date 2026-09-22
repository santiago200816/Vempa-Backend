package com.vempa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "admins")
@Getter
@Setter
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    // Nunca se guarda la contraseña en texto plano: solo su hash BCrypt.
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
}
