package com.example.Modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Compra {
    private static long contador = 1;
    private Long id;
    private Date fecha;
    private Cliente cliente;
    private List<Detalle> detalles = new ArrayList<>();
    private BigDecimal totalPagadoCOP = BigDecimal.ZERO;

    public Compra(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente es obligatorio");
        }
        this.id = contador++;
        this.fecha = new Date();
        this.cliente = cliente;
    }

    public Compra(Long id, Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente es obligatorio");
        }
        this.id = id;
        this.fecha = new Date();
        this.cliente = cliente;
        if (id != null && id >= contador) {
            contador = id + 1;
        }
    }

    public Compra(Long id, Date fecha, Cliente cliente, List<Detalle> detalles) {
        if (cliente == null || fecha == null || detalles == null) {
            throw new IllegalArgumentException("Datos de compra inválidos");
        }
        this.id = id;
        this.fecha = fecha;
        this.cliente = cliente;
        this.detalles = new ArrayList<>(detalles);
        this.totalPagadoCOP = calcularTotal();
        if (id != null && id >= contador) {
            contador = id + 1;
        }
    }

    public void agregarDetalle(Detalle detalle) {
        if (detalle == null) {
            throw new IllegalArgumentException("El detalle es obligatorio");
        }
        detalles.add(detalle);
        totalPagadoCOP = calcularTotal();
    }

    public BigDecimal calcularTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (Detalle detalle : detalles) {
            total = total.add(detalle.getSubtotal());
        }
        return total;
    }

    public Long getId() {
        return id;
    }

    public Date getFecha() {
        return fecha;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public List<Detalle> getDetalles() {
        return detalles;
    }

    public BigDecimal getTotalPagadoCOP() {
        return calcularTotal();
    }

    // Compatibilidad con el modelo antiguo.
    public Long getClienteId() {
        return cliente.getId();
    }

}
