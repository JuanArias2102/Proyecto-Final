package com.example.Modelo;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
public class Inventario {

private List<Producto> productos = new ArrayList<>();

    public void agregarProducto(Producto producto) {
        if (buscarPorId(producto.getId()) != null) {
            throw new IllegalStateException("Ya existe un producto con ese id");
        }
        productos.add(producto);
    }

    public void actualizarProducto(Producto producto) {
        for (int i = 0; i < productos.size(); i++) {
            if (productos.get(i).getId().equals(producto.getId())) {
                productos.set(i, producto);
                return;
            }
        }
        throw new IllegalArgumentException("El producto no está registrado");
    }

    public void retirarProducto(Producto producto) {
        Producto p = buscarPorId(producto.getId());
        if (p == null) throw new IllegalArgumentException("El producto no está registrado");
        p.setDisponible(false);
    }

    public boolean verificarDisponibilidad(Producto producto) {
        Producto p = buscarPorId(producto.getId());
        return p != null && p.isDisponible();
    }

    public List<Producto> consultarDisponibles() {
        return productos.stream().filter(Producto::isDisponible).collect(Collectors.toList());
    }

    public Producto buscarPorId(Long id) {
        for (Producto p : productos) {
            if (p.getId().equals(id)) return p;
        }
        return null;
    }

   
    public List<Carta> obtenerCartas() {
        List<Carta> cartas = new ArrayList<>();
        for (Producto p : productos) {
            if (p instanceof Carta){
                 cartas.add((Carta) p);
            }
        }
        return cartas;
    }

    public List<Producto> getProductos() {
         return productos; 
        }
}
