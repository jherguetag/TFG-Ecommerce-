package com.hergueta.ecommerce.config;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Encriptador
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()) // Desactivado para evitar errores en pruebas
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/**").hasRole("ADMIN") // Solo admin
                .requestMatchers("/carrito/**", "/resena/**", "/checkout/**").authenticated() // Solo logueados
                .anyRequest().permitAll() // Resto público
            )
            .formLogin(login -> login
                .loginPage("/login")
                .loginProcessingUrl("/loguear")
                .usernameParameter("email")
                .passwordParameter("pass")
                // MAGIA: Si es Admin va al panel, si es cliente a la tienda
                .successHandler((request, response, authentication) -> {
                    boolean isAdmin = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                    if (isAdmin) response.sendRedirect("/admin");
                    else response.sendRedirect("/");
                })
                .permitAll()
            )
            .logout(logout -> logout.logoutSuccessUrl("/").permitAll());

        return http.build();
    }
}