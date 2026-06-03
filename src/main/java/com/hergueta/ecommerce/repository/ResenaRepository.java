package com.hergueta.ecommerce.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.hergueta.ecommerce.model.Producto;
import com.hergueta.ecommerce.model.Resena;

public interface ResenaRepository extends JpaRepository<Resena, Long> {
    List<Resena> findByProducto(Producto producto);
}