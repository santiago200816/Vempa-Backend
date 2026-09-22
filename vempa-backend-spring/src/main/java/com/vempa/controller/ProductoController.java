package com.vempa.controller;

import com.vempa.dto.ProductoRequest;
import com.vempa.dto.ProductoResponse;
import com.vempa.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // ---------- Público ----------
    @GetMapping("/productos")
    public List<ProductoResponse> catalogoPublico() {
        return productoService.listarPublico();
    }

    @GetMapping("/productos/{id}")
    public ProductoResponse detalle(@PathVariable Long id) {
        return productoService.obtenerPorId(id);
    }

    // ---------- Admin (protegido por JwtAuthFilter + SecurityConfig) ----------
    @GetMapping("/admin/productos")
    public List<ProductoResponse> listarTodos() {
        return productoService.listarTodosAdmin();
    }

    @PostMapping("/admin/productos")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoResponse crear(@Valid @RequestBody ProductoRequest request) {
        return productoService.crear(request);
    }

    @PutMapping("/admin/productos/{id}")
    public ProductoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest request) {
        return productoService.actualizar(id, request);
    }

    @DeleteMapping("/admin/productos/{id}")
    public Map<String, Boolean> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return Map.of("ok", true);
    }
}
