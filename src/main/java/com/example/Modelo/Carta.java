package com.example.Modelo;

import java.math.BigDecimal;
public class Carta extends Producto {
private Rareza rareza;

    public Carta(Long id, String nombre, BigDecimal precioBaseUSD, Rareza rareza) {
        super(id, nombre, precioBaseUSD);
        if (rareza == null) throw new IllegalArgumentException("La rareza es obligatoria");
        this.rareza = rareza;
    }

    public Rareza getRareza() { 
        return rareza;
     }
}
