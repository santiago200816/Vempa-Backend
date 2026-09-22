package com.vempa.service;

import com.vempa.dto.LoginRequest;
import com.vempa.dto.LoginResponse;
import com.vempa.model.Admin;
import com.vempa.repository.AdminRepository;
import com.vempa.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AdminRepository adminRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        Admin admin = adminRepository.findByEmail(request.email())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos."));

        // bcrypt.matches recalcula el hash de la contraseña recibida usando la
        // MISMA sal que quedó guardada dentro de admin.getPasswordHash() (el
        // hash de BCrypt lleva la sal incrustada), y compara los resultados.
        // Nunca se desencripta el hash guardado: BCrypt no es reversible.
        if (!passwordEncoder.matches(request.password(), admin.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos.");
        }

        String token = jwtService.generarToken(admin.getEmail(), "ADMIN");
        return new LoginResponse(token, admin.getEmail(), jwtService.expiracionEnSegundos());
    }
}
