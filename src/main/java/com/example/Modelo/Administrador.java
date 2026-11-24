package com.example.Modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Administrador extends Usuario {

    public Administrador(Long id, String username, String password) {
        super(id, username, password, Rol.ADMIN);
    }

    
    public Booster crearBooster(Long id, String nombre, BigDecimal precioBaseUSD, List<Producto> productos) {
        Booster booster = new Booster(id, nombre, precioBaseUSD);
        List<Carta> cartas = new ArrayList<>();
        for (Producto producto : productos) {
            if (producto instanceof Carta) {
                cartas.add((Carta) producto);
            }
        }
        booster.generarCartas(cartas);
        gestionarProducto(booster, productos);
        return booster;
    }

    public void gestionarProducto(Producto producto, List<Producto> productos) {
        for (int i = 0; i < productos.size(); i++) {
            if (productos.get(i).getId().equals(producto.getId())) {
                productos.set(i, producto);
                return;
            }
        }
        productos.add(producto);
    }

    public Torneo crearTorneo(Long id, String nombre, Date fechaLimiteInscripcion, Integer cupoMaximo) {
        return new Torneo(id, nombre, fechaLimiteInscripcion, cupoMaximo, getId());
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

    public Cliente buscarCliente(Long id, List<Usuario> usuarios) {
        for (Usuario usuario : usuarios) {
            if (usuario instanceof Cliente && usuario.getId().equals(id)) {
                return (Cliente) usuario;
            }
        }
        return null;
    }

    public void actualizarCliente(Cliente cliente, String username, String password, List<Usuario> usuarios) {
        if (username != null) {
            for (Usuario usuario : usuarios) {
                if (usuario != cliente && usuario.getUsername().equals(username)) {
                    throw new IllegalStateException("El usuario ya existe");
                }
            }
            cliente.setUsername(username);
        }
        if (password != null) {
            cliente.setPassword(password);
        }
    }

    public void eliminarCliente(Cliente cliente, List<Usuario> usuarios, List<Torneo> torneos) {
        usuarios.remove(cliente);
        for (Torneo torneo : torneos) {
            torneo.eliminarParticipante(cliente.getId());
        }
        cliente.getTorneosInscritos().clear();
    }
}
