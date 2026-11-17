package com.example.Modelo;
import java.math.BigDecimal;
import java.math.RoundingMode;
public abstract class Producto {
 private Long id;
    private String nombre;
    private BigDecimal precioBaseUSD;
    private boolean disponible;

    public Producto(Long id, String nombre, BigDecimal precioBaseUSD) {
        if (nombre == null || nombre.isBlank() || precioBaseUSD == null
                || precioBaseUSD.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Datos del producto inválidos");
        }
        this.id = id;
        this.nombre = nombre;
        this.precioBaseUSD = precioBaseUSD;
        this.disponible = true;
    }

    public BigDecimal calcularPrecioCOP(BigDecimal tasaCambio) {
        return precioBaseUSD.multiply(tasaCambio).setScale(2, RoundingMode.HALF_UP);
    }
    public Long getId() { 
        return id; 
    }
    public String getNombre() { 
        return nombre; 
    }
    public BigDecimal getPrecioBaseUSD() { 
        return precioBaseUSD; 
    }
    public boolean isDisponible() { 
        return disponible; 
    }
    public void setDisponible(boolean disponible) { 
        this.disponible = disponible; 
    }
}
