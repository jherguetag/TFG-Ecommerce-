package com.hergueta.ecommerce.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import com.hergueta.ecommerce.model.Usuario;
import com.hergueta.ecommerce.repository.UsuarioRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario == null) throw new UsernameNotFoundException("Usuario no encontrado");
        
        return User.withUsername(usuario.getEmail())
                   .password(usuario.getContrasena())
                   .roles(usuario.getRol())
                   .build();
    }
}