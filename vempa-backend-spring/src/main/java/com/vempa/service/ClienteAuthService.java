package com.vempa.service;

import com.vempa.dto.ClienteAuthResponse;
import com.vempa.dto.LoginRequest;
import com.vempa.dto.RegistroClienteRequest;
import com.vempa.model.Usuario;
import com.vempa.repository.UsuarioRepository;
import com.vempa.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ClienteAuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public ClienteAuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public ClienteAuthResponse registrar(RegistroClienteRequest req) {
        if (usuarioRepository.existsByEmail(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese correo.");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(req.nombre());
        usuario.setEmail(req.email());
        usuario.setTelefono(req.telefono());
        // Igual que con el admin: solo se guarda el HASH de BCrypt, nunca la
        // contraseña en texto plano.
        usuario.setPasswordHash(passwordEncoder.encode(req.password()));
        usuarioRepository.save(usuario);

        return generarRespuesta(usuario);
    }

    public ClienteAuthResponse login(LoginRequest req) {
        Usuario usuario = usuarioRepository.findByEmail(req.email())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos."));

        if (!passwordEncoder.matches(req.password(), usuario.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos.");
        }

        return generarRespuesta(usuario);
    }

    private ClienteAuthResponse generarRespuesta(Usuario usuario) {
        String token = jwtService.generarToken(usuario.getEmail(), "CLIENTE");
        return new ClienteAuthResponse(
            token, usuario.getEmail(), usuario.getNombre(), usuario.getTelefono(),
            jwtService.expiracionEnSegundos()
        );
    }
}
