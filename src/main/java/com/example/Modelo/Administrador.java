package com.example.Modelo;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class Administrador extends Usuario {

    public Administrador(Long id, String username, String password) {
        super(id, username, password, Rol.ADMIN);
    }

    
    public Booster crearBooster(Long id, String nombre, BigDecimal precioBaseUSD, Inventario inventario) {
        Booster booster = new Booster(id, nombre, precioBaseUSD);
        booster.generarCartas(inventario.obtenerCartas());
        inventario.agregarProducto(booster);
        return booster;
    }

    public void gestionarProducto(Producto producto, Inventario inventario) {
        if (inventario.buscarPorId(producto.getId()) == null) {
            inventario.agregarProducto(producto);
        } else {
            inventario.actualizarProducto(producto);
        }
    }

    public Torneo crearTorneo(Long id, String nombre, Date fecha,
                              Date fechaLimiteInscripcion, Integer cupoMaximo) {
        return new Torneo(id, nombre, fecha, fechaLimiteInscripcion, cupoMaximo);
    }

    /** Solo el admin registra clientes. */
    public Cliente registrarCliente(Long id, String username, String password, List<Usuario> usuarios) {
        for (Usuario u : usuarios) {
            if (u.getUsername().equals(username)) {
                throw new IllegalStateException("El usuario ya existe");
            }
        }
        Cliente cliente = new Cliente(id, username, password);
        usuarios.add(cliente);
        return cliente;
    }
}
