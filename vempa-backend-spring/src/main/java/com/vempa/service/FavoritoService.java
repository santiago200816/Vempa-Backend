package com.vempa.service;

import com.vempa.dto.ProductoResponse;
import com.vempa.model.Favorito;
import com.vempa.model.Producto;
import com.vempa.model.Usuario;
import com.vempa.repository.FavoritoRepository;
import com.vempa.repository.ProductoRepository;
import com.vempa.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;

    public FavoritoService(FavoritoRepository favoritoRepository, UsuarioRepository usuarioRepository, ProductoRepository productoRepository) {
        this.favoritoRepository = favoritoRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
    }

    private Usuario obtenerUsuario(String email) {
        return usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Cliente no encontrado."));
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> listar(String email) {
        Usuario usuario = obtenerUsuario(email);
        return favoritoRepository.findByUsuarioIdOrderByCreatedAtDesc(usuario.getId()).stream()
            .map(f -> ProductoResponse.desde(f.getProducto()))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<Long> listarIds(String email) {
        Usuario usuario = obtenerUsuario(email);
        return favoritoRepository.findByUsuarioId(usuario.getId()).stream()
            .map(f -> f.getProducto().getId())
            .toList();
    }

    @Transactional
    public void agregar(String email, Long productoId) {
        Usuario usuario = obtenerUsuario(email);

        if (favoritoRepository.existsByUsuarioIdAndProductoId(usuario.getId(), productoId)) {
            return; // ya estaba en favoritos, no hacemos nada
        }

        Producto producto = productoRepository.findById(productoId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado."));

        Favorito favorito = new Favorito();
        favorito.setUsuario(usuario);
        favorito.setProducto(producto);
        favoritoRepository.save(favorito);
    }

    @Transactional
    public void quitar(String email, Long productoId) {
        Usuario usuario = obtenerUsuario(email);
        favoritoRepository.findByUsuarioIdAndProductoId(usuario.getId(), productoId)
            .ifPresent(favoritoRepository::delete);
    }
}
