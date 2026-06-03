package com.hergueta.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.hergueta.ecommerce.model.DetallePedido;
import com.hergueta.ecommerce.model.Pedido;
import com.hergueta.ecommerce.model.Producto;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {
	// Busca si ya existe una fila con este producto en este pedido concreto
	DetallePedido findByPedidoAndProducto(Pedido pedido, Producto producto);
}