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
    
    @PostMapping("/carrito/remove-producto/{idProducto}")
    public String quitarProductoPorId(@PathVariable Long idProducto, Principal principal) {
        Usuario usuario = usuarioRepository.findByEmail(principal.getName());
        Pedido carrito = pedidoRepository.findByUsuarioAndEstado(usuario, "CARRITO");
        
        if (carrito != null) {
            Producto producto = productoRepository.findById(idProducto).orElse(null);
            
            // Buscamos si ese producto está en el carrito
            DetallePedido detalle = detallePedidoRepository.findByPedidoAndProducto(carrito, producto);
            
            if (detalle != null) {
                // Si la cantidad es mayor que 1, le restamos 1
                if (detalle.getCantidad() > 1) {
                    detalle.setCantidad(detalle.getCantidad() - 1);
                    detallePedidoRepository.save(detalle);
                } else {
                    // Si solo hay 1, borramos el detalle entero del carrito
                    detallePedidoRepository.delete(detalle);
                }
            }
        }
        return "redirect:/"; // Da igual porque fetch lo procesa en segundo plano
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
    
 // 1. Procesa el formulario de pago
    @PostMapping("/checkout/procesar")
    public String procesarPago(@RequestParam String direccion, @RequestParam String fechaEntrega, Principal principal) {
        Usuario usuario = usuarioRepository.findByEmail(principal.getName());
        Pedido carrito = pedidoRepository.findByUsuarioAndEstado(usuario, "CARRITO");

        if (carrito != null) {
            carrito.setDireccion(direccion);
            // Convertimos el String del calendario a fecha real de Java
            carrito.setFechaEntrega(java.time.LocalDate.parse(fechaEntrega));
            
            // Calculamos el total para guardarlo para siempre
            double total = 0.0;
            for (DetallePedido detalle : carrito.getDetalles()) {
                total += detalle.getProducto().getPrecio() * detalle.getCantidad();
            }
            carrito.setTotal(total);
            
            // ¡MAGIA! Cambiamos el estado. Ya no es un carrito, es un pedido en firme.
            carrito.setEstado("EN CAMINO"); 
            pedidoRepository.save(carrito);
        }
        return "redirect:/pedidos?exito=true";
    }

    // 2. Muestra la página de historial (Mis Pedidos)
 // Muestra la página de historial (Mis Pedidos) con actualización automática
    @GetMapping("/pedidos")
    public String verPedidos(Model model, Principal principal) {
        Usuario usuario = usuarioRepository.findByEmail(principal.getName());
        
        // 1. Traemos todos los pedidos del usuario (que no sean el carrito activo)
        List<Pedido> misPedidos = pedidoRepository.findAll().stream()
                .filter(p -> p.getUsuario().getId().equals(usuario.getId()) && !p.getEstado().equals("CARRITO"))
                .toList();

        // 2. MAGIA DEL TIEMPO: Comprobamos si alguno ya debería estar entregado
        java.time.LocalDate hoy = java.time.LocalDate.now(); // Qué día es hoy en la vida real
        
        for (Pedido pedido : misPedidos) {
            // Si el pedido pone "EN CAMINO", pero la fecha de entrega ya es HOY o fue en el PASADO
            if ("EN CAMINO".equals(pedido.getEstado()) && 
               (pedido.getFechaEntrega().isBefore(hoy) || pedido.getFechaEntrega().isEqual(hoy))) {
                
                // Lo actualizamos automáticamente
                pedido.setEstado("ENTREGADO");
                pedidoRepository.save(pedido);
            }
        }

        // 3. Mandamos la lista (ya actualizada) a la pantalla
        model.addAttribute("pedidos", misPedidos);
        return "pedidos";
    }
}