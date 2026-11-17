package com.example.Modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Cliente extends Usuario {
    
    private List<Compra> historialCompras = new ArrayList<>();

    public Cliente(Long id, String username, String password) {
        super(id, username, password, Rol.CLIENTE);
    }

    public List<Producto> consultarCatalogo(Inventario inventario) {
        return inventario.consultarDisponibles();
    }

    public Compra realizarCompra(Long idCompra, List<Producto> productos, Inventario inventario,
                                 BigDecimal tasaCambio, String fuenteTasa) {
        Compra compra = new Compra(idCompra, getId());
        for (Producto p : productos) {
            compra.agregarProducto(p);
        }
        compra.confirmarCompra(inventario, tasaCambio, fuenteTasa);
        historialCompras.add(compra);
        return compra;
    }

    public List<Carta> abrirBooster(Booster booster) {
        return booster.abrirBooster(getId());
    }

    public boolean inscribirseEnTorneo(Torneo torneo) {
        return torneo.inscribirUsuario(this);
    }

    public List<Compra> consultarHistorial() {
        return historialCompras;
    }

    public List<Compra> getHistorialCompras() {
         return historialCompras; 
        }
}

