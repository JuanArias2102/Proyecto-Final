package com.example.Modelo;

import java.util.ArrayList;
import java.util.List;

public class Tienda {
    private final List<Producto> productos = new ArrayList<>();
    private final List<Usuario> usuarios = new ArrayList<>();
    private final List<Torneo> torneos = new ArrayList<>();

    public List<Producto> getProductos() {

        return productos;
    }

    public List<Usuario> getUsuarios() {

        return usuarios;
    }

    public List<Torneo> getTorneos() {

        return torneos;
    }

    public Long nuevoId() {
        long maximo = 0;
        for (Producto producto : productos) {
            maximo = Math.max(maximo, producto.getId());
        }
        for (Usuario usuario : usuarios) {
            maximo = Math.max(maximo, usuario.getId());
            if (usuario instanceof Cliente) {
                for (Compra compra : ((Cliente) usuario).getHistorialCompras()) {
                    maximo = Math.max(maximo, compra.getId());
                }
            }
        }
        for (Torneo torneo : torneos) {
            maximo = Math.max(maximo, torneo.getId());
        }
        return maximo + 1;
    }
}
