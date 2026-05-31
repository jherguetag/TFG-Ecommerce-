package com.hergueta.ecommerce.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hergueta.ecommerce.model.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
	//Buscar por palabras
	List<Producto> findByNombreContainingIgnoreCase(String palabra);
}
