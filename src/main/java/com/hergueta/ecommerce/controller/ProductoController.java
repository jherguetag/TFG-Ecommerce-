package com.hergueta.ecommerce.controller;

import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hergueta.ecommerce.model.Producto;
import com.hergueta.ecommerce.model.Resena;
import com.hergueta.ecommerce.model.Usuario;
import com.hergueta.ecommerce.repository.ProductoRepository;
import com.hergueta.ecommerce.repository.ResenaRepository;
import com.hergueta.ecommerce.repository.UsuarioRepository;

@Controller
public class ProductoController {

    @Autowired private ProductoRepository productoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ResenaRepository resenaRepository;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("productos", productoRepository.findAll());
        return "index";
    }

    // --- PANEL DE ADMINISTRACIÓN ---
    // Ya no hay "if", Spring Security bloquea a los que no son ADMIN antes de llegar aquí
    @GetMapping("/admin")
    public String panelAdmin(Model model) {
        model.addAttribute("productos", productoRepository.findAll());
        return "admin";
    }

    @GetMapping("/admin/nuevo")
    public String formularioNuevo(Model model) {
        model.addAttribute("producto", new Producto());
        return "formulario";
    }

    @PostMapping("/admin/guardar")
    public String guardarProducto(@ModelAttribute Producto producto) {
        productoRepository.save(producto);
        return "redirect:/admin";
    }

    @GetMapping("/admin/editar/{id}")
    public String editarProducto(@PathVariable Long id, Model model) {
        Producto producto = productoRepository.findById(id).orElse(null);
        model.addAttribute("producto", producto);
        return "formulario";
    }

    @GetMapping("/admin/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id) {
        productoRepository.deleteById(id);
        return "redirect:/admin";
    }

    // --- LOGIN Y REGISTRO ---
    @GetMapping("/login")
    public String login() {
        return "login"; // Muestra el login.html
    }

    // NUEVO: Muestra la pantalla separada de registro
    @GetMapping("/registro")
    public String mostrarRegistro() {
        return "registro"; // Muestra el registro.html
    }

    // MODIFICADO: Procesa los datos cuando le dan a "Crear cuenta"
    @PostMapping("/registro")
    public String registrar(@RequestParam String email, @RequestParam String pass) {
        String passEncriptada = passwordEncoder.encode(pass); 
        Usuario nuevoUsuario = new Usuario(email, passEncriptada, "CLIENTE");
        usuarioRepository.save(nuevoUsuario);
        return "redirect:/login?registrado=true";
    }

    

    // --- BUSCADOR ---
    @GetMapping("/buscar")
    public String buscarProducto(@RequestParam String palabra, Model model) {
        model.addAttribute("productos", productoRepository.findByNombreContainingIgnoreCase(palabra));
        return "index";
    }

    // --- AÑADIR RESEÑA ---
    @PostMapping("/resena/add/{idProducto}")
    public String añadirResena(@PathVariable Long idProducto, @RequestParam String comentario, Principal principal) {
        Usuario usuario = usuarioRepository.findByEmail(principal.getName());
        Producto producto = productoRepository.findById(idProducto).orElse(null);
        
        Resena nuevaResena = new Resena();
        nuevaResena.setUsuario(usuario);
        nuevaResena.setProducto(producto);
        nuevaResena.setComentario(comentario);
        resenaRepository.save(nuevaResena);
        return "redirect:/";
    }
}
