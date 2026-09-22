package com.vempa.repository;

import com.vempa.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByActivoTrueOrderByCreatedAtDesc();
    List<Producto> findAllByOrderByCreatedAtDesc();
}
