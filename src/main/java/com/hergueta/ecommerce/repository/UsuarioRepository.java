package com.hergueta.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.hergueta.ecommerce.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Usuario findByEmail(String email);
}
