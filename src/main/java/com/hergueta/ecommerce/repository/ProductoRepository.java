package com.hergueta.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.hergueta.ecommerce.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
