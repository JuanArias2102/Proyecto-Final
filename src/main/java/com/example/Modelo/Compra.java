package com.example.Modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Compra {
 private Long id;
    private Date fecha;
    private Long clienteId;
    private List<Producto> productos = new ArrayList<>();
    private BigDecimal totalPagadoCOP = BigDecimal.ZERO;
    private Factura factura;

    public Compra(Long id, Long clienteId) {
        this.id = id;
        this.clienteId = clienteId;
        this.fecha = new Date();
    }

    public void agregarProducto(Producto producto) {
        productos.add(producto);
    }

    public BigDecimal calcularTotal(BigDecimal tasaCambio) {
        BigDecimal total = BigDecimal.ZERO;
        for (Producto p : productos) {
            total = total.add(p.calcularPrecioCOP(tasaCambio));
        }
        return total;
    }

    
    public Factura confirmarCompra(Inventario inventario, BigDecimal tasaCambio, String fuenteTasa) {
        if (productos.isEmpty()) {
            throw new IllegalArgumentException("La compra no tiene productos");
        }
        for (Producto p : productos) {
            if (!inventario.verificarDisponibilidad(p)) {
                throw new IllegalStateException("El producto '" + p.getNombre() + "' ya no está disponible");
            }
        }
        this.totalPagadoCOP = calcularTotal(tasaCambio);
        for (Producto p : productos) {
            if (p instanceof Booster) {
                ((Booster) p).marcarVendido(clienteId);
            }
            inventario.retirarProducto(p);
        }
        this.factura = Factura.generarFactura(this, tasaCambio, fuenteTasa);
        return factura;
    }

    public Long getId() { 
        return id; 

    }
    public Date getFecha() { 
        return fecha;

     }
    public Long getClienteId() { 
        return clienteId; 

    }
    public List<Producto> getProductos() { 
        return productos;

     }
    public BigDecimal getTotalPagadoCOP() { 
        return totalPagadoCOP; 

    }
    public Factura getFactura() { 
        return factura; 

    }
}
