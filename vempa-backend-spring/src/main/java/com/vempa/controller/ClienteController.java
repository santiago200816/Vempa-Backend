package com.vempa.controller;

import com.vempa.dto.ClienteAuthResponse;
import com.vempa.dto.LoginRequest;
import com.vempa.dto.ProductoResponse;
import com.vempa.dto.RegistroClienteRequest;
import com.vempa.service.ClienteAuthService;
import com.vempa.service.FavoritoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteAuthService clienteAuthService;
    private final FavoritoService favoritoService;

    public ClienteController(ClienteAuthService clienteAuthService, FavoritoService favoritoService) {
        this.clienteAuthService = clienteAuthService;
        this.favoritoService = favoritoService;
    }

    // ---------- Público ----------
    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteAuthResponse registrar(@Valid @RequestBody RegistroClienteRequest request) {
        return clienteAuthService.registrar(request);
    }

    @PostMapping("/login")
    public ClienteAuthResponse login(@Valid @RequestBody LoginRequest request) {
        return clienteAuthService.login(request);
    }

    // ---------- Protegido (ROLE_CLIENTE) ----------
    // El email del cliente autenticado viene del propio JWT validado por
    // JwtAuthFilter — Spring lo inyecta aquí como el "name" de Authentication.
    @GetMapping("/favoritos")
    public List<ProductoResponse> misFavoritos(Authentication auth) {
        return favoritoService.listar(auth.getName());
    }

    @GetMapping("/favoritos/ids")
    public List<Long> misFavoritosIds(Authentication auth) {
        return favoritoService.listarIds(auth.getName());
    }

    @PostMapping("/favoritos/{productoId}")
    public Map<String, Boolean> agregarFavorito(Authentication auth, @PathVariable Long productoId) {
        favoritoService.agregar(auth.getName(), productoId);
        return Map.of("ok", true);
    }

    @DeleteMapping("/favoritos/{productoId}")
    public Map<String, Boolean> quitarFavorito(Authentication auth, @PathVariable Long productoId) {
        favoritoService.quitar(auth.getName(), productoId);
        return Map.of("ok", true);
    }
}
