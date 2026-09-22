package com.vempa.config;

import com.vempa.model.Admin;
import com.vempa.repository.AdminRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${vempa.admin.email:}")
    private String adminEmail;

    @Value("${vempa.admin.password:}")
    private String adminPassword;

    public AdminInitializer(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            log.info("ADMIN_EMAIL/ADMIN_PASSWORD no configurados: no se crea admin automático.");
            return;
        }

        if (adminRepository.findByEmail(adminEmail).isPresent()) {
            log.info("El admin '{}' ya existe, no se vuelve a crear.", adminEmail);
            return;
        }

        Admin admin = new Admin();
        admin.setEmail(adminEmail);
        // Se guarda el HASH de BCrypt, nunca la contraseña en texto plano.
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        adminRepository.save(admin);
        log.info("Admin creado automáticamente: {}", adminEmail);
    }
}
