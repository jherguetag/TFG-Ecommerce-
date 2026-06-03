package com.hergueta.ecommerce.controller;
import java.security.Principal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.hergueta.ecommerce.model.*;
import com.hergueta.ecommerce.repository.*;

@Controller
public class CarritoController {

    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private DetallePedidoRepository detallePedidoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ProductoRepository productoRepository;

    @GetMapping("/carrito")
    public String verCarrito(Model model, Principal principal) {
        Usuario usuario = usuarioRepository.findByEmail(principal.getName());
        Pedido carrito = pedidoRepository.findByUsuarioAndEstado(usuario, "CARRITO");
        model.addAttribute("carrito", carrito);

        // LÓGICA DE SUGERENCIAS INTELIGENTES:
        if (carrito != null && carrito.getDetalles() != null && !carrito.getDetalles().isEmpty()) {
            var categoriaDestacada = carrito.getDetalles().get(0).getProducto().getCategoria();
            
            // Filtramos para obtener productos de la categoría QUE NO ESTÉN YA en el carrito
            List<Producto> sugerencias = productoRepository.findByCategoria(categoriaDestacada);
            List<Long> idsEnCarrito = carrito.getDetalles().stream()
                                            .map(d -> d.getProducto().getId())
                                            .toList();
            
            sugerencias.removeIf(p -> idsEnCarrito.contains(p.getId()));
            model.addAttribute("sugerencias", sugerencias);
        }
        
        return "carrito";
    }
    
    @PostMapping("/carrito/update")
    public String actualizarCantidad(@RequestParam Long idDetalle, @RequestParam int cantidad) {
        DetallePedido detalle = detallePedidoRepository.findById(idDetalle).orElse(null);
        if (detalle != null) {
            detalle.setCantidad(cantidad);
            detallePedidoRepository.save(detalle);
        }
        return "redirect:/carrito";
    }

    @PostMapping("/carrito/add/{idProducto}")
    public String añadirAlCarrito(@PathVariable Long idProducto, Principal principal) {
        Usuario usuario = usuarioRepository.findByEmail(principal.getName());
        
        // 1. Buscamos o creamos el carrito
        Pedido carrito = pedidoRepository.findByUsuarioAndEstado(usuario, "CARRITO");
        if (carrito == null) {
            carrito = new Pedido();
            carrito.setUsuario(usuario);
            carrito.setEstado("CARRITO");
            pedidoRepository.save(carrito);
        }

        // 2. Buscamos el producto
        Producto producto = productoRepository.findById(idProducto).orElse(null);
        
        // 3. ¡AQUÍ ESTÁ LA MAGIA! Buscamos si ya existe este producto en el carrito
        DetallePedido detalle = detallePedidoRepository.findByPedidoAndProducto(carrito, producto);

        if (detalle != null) {
            // Si ya existe, solo sumamos 1 a la cantidad
            detalle.setCantidad(detalle.getCantidad() + 1);
        } else {
            // Si no existe, creamos el nuevo detalle
            detalle = new DetallePedido();
            detalle.setPedido(carrito);
            detalle.setProducto(producto);
            detalle.setCantidad(1);
        }
        
        // 4. Guardamos el cambio
        detallePedidoRepository.save(detalle);

        return "redirect:/carrito";
    }
    
    @GetMapping("/carrito/eliminar/{idDetalle}")
    public String eliminarDelCarrito(@PathVariable Long idDetalle) {
        detallePedidoRepository.deleteById(idDetalle);
        return "redirect:/carrito";
    }
    
 // 3. Ir a sección de pago
    @GetMapping("/checkout")
    public String irAPago(Model model, Principal principal) {
        Usuario usuario = usuarioRepository.findByEmail(principal.getName());
        Pedido carrito = pedidoRepository.findByUsuarioAndEstado(usuario, "CARRITO");

        // Si el carrito está vacío, no le dejamos ir a pagar
        if (carrito == null || carrito.getDetalles().isEmpty()) {
            return "redirect:/carrito";
        }

        // Calculamos el dinero total sumando los precios
        double total = 0.0;
        for (DetallePedido detalle : carrito.getDetalles()) {
            total += detalle.getProducto().getPrecio() * detalle.getCantidad();
        }

        model.addAttribute("carrito", carrito);
        model.addAttribute("total", Math.round(total * 100.0) / 100.0); // Redondeado a 2 decimales
        
        return "pago"; // Abrimos tu archivo pago.html
    }
}