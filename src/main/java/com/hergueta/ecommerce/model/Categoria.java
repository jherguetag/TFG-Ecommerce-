package com.hergueta.ecommerce.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    // Relación: Una categoría tiene muchos productos 
    @OneToMany(mappedBy = "categoria")
    private List<Producto> productos;

    // Constructor
    public Categoria() {
    	
    }
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}