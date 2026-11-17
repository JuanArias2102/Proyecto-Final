package com.example.Modelo;

import java.math.BigDecimal;
import java.util.Date;
public class Factura {
    private static long contador = 1;
    private Long numeroFactura;
    private Date fecha;
    private Long compraId;
    private BigDecimal totalCOP;
    private BigDecimal tasaCambio;
    private String fuenteTasa; 

    private Factura(Long compraId, BigDecimal totalCOP, BigDecimal tasaCambio, String fuenteTasa) {
        this.numeroFactura = contador++;
        this.fecha = new Date();
        this.compraId = compraId;
        this.totalCOP = totalCOP;
        this.tasaCambio = tasaCambio;
        this.fuenteTasa = fuenteTasa;
    }

    public static Factura generarFactura(Compra compra, BigDecimal tasaCambio, String fuenteTasa) {
        return new Factura(compra.getId(), compra.getTotalPagadoCOP(), tasaCambio, fuenteTasa);
    }

    public Long getNumeroFactura() { 
        return numeroFactura; 
    }
    public Date getFecha() { 
        return fecha; 
    }
    public Long getCompraId() { 
        return compraId; 
    }
    public BigDecimal getTotalCOP() { 
        return totalCOP; 

    }
    public BigDecimal getTasaCambio() { 
        return tasaCambio; 
    }
    public String getFuenteTasa() { 
        return fuenteTasa; 
    }
}
