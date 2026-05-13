package com.hergueta.ecommerce.controller;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.hergueta.ecommerce.repository.ProductoRepository;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import com.hergueta.ecommerce.model.Producto;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProductoController {

    @Autowired
    private ProductoRepository productoRepository;
    
    private String rolActivo = "INVITADO";

    @GetMapping("/")
    public String home(Model model) {
    	
        // Manda la lista de productos a la vista HTML
        model.addAttribute("productos", productoRepository.findAll());
        return "index"; 
    }
    
 // Mostrar el panel de administración
    @GetMapping("/admin")
    public String panelAdmin(Model model) {
    	if (!"ADMIN".equals(rolActivo)) return "redirect:/login";
        model.addAttribute("productos", productoRepository.findAll());
        return "admin";
    }

    // Mostrar el formulario para añadir un producto nuevo
    @GetMapping("/admin/nuevo")
    public String formularioNuevo(Model model) {
    	if (!"ADMIN".equals(rolActivo)) return "redirect:/login";
        model.addAttribute("producto", new Producto());
        return "formulario";
    }

 // Crear un producto
    @PostMapping("/admin/guardar")
    public String guardarProducto(@ModelAttribute Producto producto) {
        if (!"ADMIN".equals(rolActivo)) return "redirect:/login";
        productoRepository.save(producto);
        return "redirect:/admin";
    }
    
 // Editar un producto
    @GetMapping("/admin/editar/{id}")
    public String editarProducto(@PathVariable Long id, Model model) {
        if (!"ADMIN".equals(rolActivo)) return "redirect:/login";
        Producto producto = productoRepository.findById(id).orElse(null);
        model.addAttribute("producto", producto);
        return "formulario";
    }

    // Eliminar un producto
    @GetMapping("/admin/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id) {
    	if (!"ADMIN".equals(rolActivo)) return "redirect:/login";
        productoRepository.deleteById(id);
        return "redirect:/admin";
    }
    
    // Muestra la página de Login
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // Proceso del login
     @PostMapping("/loguear")
    public String loguear(@RequestParam String email, @RequestParam String pass) {
        if ("admin@gmail.com".equals(email) && "1234".equals(pass)) {
            rolActivo = "ADMIN";
            return "redirect:/admin";
        } else if ("cliente@gmail.com".equals(email) && "1234".equals(pass)) {
            rolActivo = "CLIENTE";
            return "redirect:/"; // Vuelve a la tienda pero ya "fichado" como cliente
        } else {
            return "redirect:/login?error=true";
        }
    }
    
     @GetMapping("/logout")
     public String logout() {
         rolActivo = "INVITADO";
         return "redirect:/";
     }

    // Procedimiento de pago para un producto específico
    @GetMapping("/checkout/{id}")
    public String checkout(@PathVariable Long id, Model model) {
        // Si no es ni cliente ni admin, lo mandamos a loguearse
        if ("INVITADO".equals(rolActivo)) {
            return "redirect:/login";
        }
        Producto producto = productoRepository.findById(id).orElse(null);
        model.addAttribute("producto", producto);
        return "pago";
    }
    
   
}
