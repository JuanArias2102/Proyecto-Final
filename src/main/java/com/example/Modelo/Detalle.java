package com.example.Modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Detalle extends Producto {
    private final Producto producto;
    private int cantidad;
    private BigDecimal subtotal;

    public Detalle(Producto producto, int cantidad, BigDecimal tasaCambio) {
        super(validarProducto(producto).getId(), producto.getNombre(), producto.getPrecioBaseUSD());
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        this.producto = producto;
        this.cantidad = cantidad;
        setDisponible(producto.isDisponible());
        this.subtotal = calcularSubtotal(tasaCambio);
    }

    private static Producto validarProducto(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto es obligatorio");
        }
        return producto;
    }

    private Detalle(Producto producto, int cantidad, BigDecimal subtotal, boolean restaurado) {
        super(validarProducto(producto).getId(), producto.getNombre(), producto.getPrecioBaseUSD());
        if (cantidad <= 0 || subtotal == null || subtotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Datos del detalle inválidos");
        }
        this.producto = producto;
        this.cantidad = cantidad;
        this.subtotal = subtotal;
        setDisponible(producto.isDisponible());
    }

    public static Detalle restaurar(Producto producto, int cantidad, BigDecimal subtotal) {
        return new Detalle(producto, cantidad, subtotal, true);
    }

    public BigDecimal calcularSubtotal(BigDecimal tasaCambio) {
        BigDecimal precioCOP = producto.calcularPrecioCOP(tasaCambio);
        return precioCOP.multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_UP);
    }

    public Producto getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}
