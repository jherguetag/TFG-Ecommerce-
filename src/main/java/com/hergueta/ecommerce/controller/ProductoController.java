package com.hergueta.ecommerce.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hergueta.ecommerce.model.Producto;
import com.hergueta.ecommerce.repository.ProductoRepository;

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
    public String panelAdmin(Model model, RedirectAttributes redirectAttributes) {
        
        // 1. Si no ha iniciado sesión, se le manda al logn
        if ("INVITADO".equals(rolActivo)) {
            return "redirect:/login"; 
        }
        
        // 2. Si es un cliente intentando entrar en la zona de administración, se le lanza el error
        if ("CLIENTE".equals(rolActivo)) {
            redirectAttributes.addFlashAttribute("accesoDenegado", "Lo sentimos, necesitas permisos de administrador para acceder a esta sección.");
            return "redirect:/login"; 
        }

        // 3. Si es el admin, le cargamos sus productos y le dejamos entrar
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
            return "redirect:/"; // Vuelve a la tienda pero como cliente
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
     public String checkout(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
         // Si no es ni cliente ni admin, lo mandamos a loguearse con el mensaje
         if ("INVITADO".equals(rolActivo)) {
             redirectAttributes.addFlashAttribute("necesitaLogin", "Para poder realizar una compra, por favor inicia sesión con tu cuenta de cliente.");
             return "redirect:/login";
         }
         Producto producto = productoRepository.findById(id).orElse(null);
         model.addAttribute("producto", producto);
         return "pago";
     }
    
  // Buscador de productos
     @GetMapping("/buscar")
     public String buscarProducto(@RequestParam String palabra, Model model) {
         model.addAttribute("productos", productoRepository.findByNombreContainingIgnoreCase(palabra));
         return "index";
     }
}
