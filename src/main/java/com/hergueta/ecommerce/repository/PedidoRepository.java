package com.hergueta.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.hergueta.ecommerce.model.Pedido;
import com.hergueta.ecommerce.model.Usuario;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    Pedido findByUsuarioAndEstado(Usuario usuario, String estado);
}