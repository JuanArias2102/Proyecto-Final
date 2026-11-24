package com.example.Modelo;

import java.math.BigDecimal;

public class ProductoSellado extends Producto {
    private String tipo;
    private String descripcion;

    public ProductoSellado(Long id, String nombre, BigDecimal precioBaseUSD, String tipo, String descripcion) {
        super(id, nombre, precioBaseUSD);
        if (tipo == null || tipo.isBlank() || descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("Tipo y descripción son obligatorios");
        }
        this.tipo = tipo;
        this.descripcion = descripcion;
    }

    public String getTipo() {
        return tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
