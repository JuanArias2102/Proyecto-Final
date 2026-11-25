package com.example.Modelo;

import java.math.BigDecimal;

public class Accesorio extends Producto {
    private String tipoAccesorio;
    private String marca;

    public Accesorio(Long id, String nombre, BigDecimal precioBaseUSD, String tipoAccesorio, String marca) {
        super(id, nombre, precioBaseUSD);
        if (tipoAccesorio == null || tipoAccesorio.isBlank() || marca == null || marca.isBlank()) {
            throw new IllegalArgumentException("Tipo y marca del accesorio son obligatorios");
        }
        this.tipoAccesorio = tipoAccesorio;
        this.marca = marca;
    }

    public String getTipoAccesorio() {
        return tipoAccesorio;
    }

    public String getMarca() {
        return marca;
    }
}
