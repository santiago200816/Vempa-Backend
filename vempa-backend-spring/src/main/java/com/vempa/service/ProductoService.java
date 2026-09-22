package com.vempa.service;

import com.vempa.dto.ProductoRequest;
import com.vempa.dto.ProductoResponse;
import com.vempa.dto.VarianteDTO;
import com.vempa.model.Producto;
import com.vempa.model.Variante;
import com.vempa.repository.ProductoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarPublico() {
        return productoRepository.findByActivoTrueOrderByCreatedAtDesc().stream()
            .map(ProductoResponse::desde)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarTodosAdmin() {
        return productoRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(ProductoResponse::desde)
            .toList();
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorId(Long id) {
        return productoRepository.findById(id)
            .map(ProductoResponse::desde)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado."));
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest req) {
        Producto p = new Producto();
        aplicarCampos(p, req);
        return ProductoResponse.desde(productoRepository.save(p));
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest req) {
        Producto p = productoRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado."));
        aplicarCampos(p, req);
        return ProductoResponse.desde(productoRepository.save(p));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado.");
        }
        productoRepository.deleteById(id);
    }

    private void aplicarCampos(Producto p, ProductoRequest req) {
        p.setNombre(req.nombre());
        p.setDescripcion(req.descripcion());
        p.setPrecio(req.precio());
        p.setCategoria(req.categoria());
        p.setImagenUrl(req.imagenUrl());
        p.setActivo(req.activo() == null || req.activo());

        List<Variante> nuevas = (req.variantes() == null ? List.<VarianteDTO>of() : req.variantes()).stream()
            .filter(v -> v.color() != null && !v.color().isBlank())
            .map(v -> {
                Variante variante = new Variante();
                variante.setColor(v.color());
                variante.setTalla(v.talla());
                variante.setStock(v.stock());
                return variante;
            })
            .toList();

        p.reemplazarVariantes(nuevas);
    }
}
