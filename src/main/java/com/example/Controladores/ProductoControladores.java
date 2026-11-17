package com.example.Controladores;
import com.example.Modelo.Accesorio;
import com.example.Modelo.Administrador;
import com.example.Modelo.Booster;
import com.example.Modelo.Carta;
import com.example.Modelo.Producto;
import com.example.Modelo.ProductoSellado;
import com.example.Modelo.Rareza;

import java.math.BigDecimal;
import java.util.List;
public class ProductoControladores {
public Carta crearCarta(Administrador admin, String nombre, BigDecimal precioBaseUSD, String rareza) {
        Carta carta = new Carta(Sistema.datos.nuevoId(), nombre, precioBaseUSD, leerRareza(rareza));
        admin.gestionarProducto(carta, Sistema.datos.getInventario());
        Sistema.guardar();
        return carta;
    }

    public ProductoSellado crearSellado(Administrador admin, String nombre, BigDecimal precioBaseUSD,
                                        String tipo, String descripcion) {
        ProductoSellado sellado = new ProductoSellado(
                Sistema.datos.nuevoId(), nombre, precioBaseUSD, tipo, descripcion);
        admin.gestionarProducto(sellado, Sistema.datos.getInventario());
        Sistema.guardar();
        return sellado;
    }

    public Accesorio crearAccesorio(Administrador admin, String nombre, BigDecimal precioBaseUSD,
                                    String tipoAccesorio, String marca) {
        Accesorio accesorio = new Accesorio(
                Sistema.datos.nuevoId(), nombre, precioBaseUSD, tipoAccesorio, marca);
        admin.gestionarProducto(accesorio, Sistema.datos.getInventario());
        Sistema.guardar();
        return accesorio;
    }

    
    public List<Producto> listarProductos() {
        return Sistema.datos.getInventario().consultarDisponibles();
    }

    
    public Producto buscarProducto(Long id) {
        Producto producto = Sistema.datos.getInventario().buscarPorId(id);
        if (producto == null) {
            throw new NoEncontradoException("El producto no existe");
        }
        return producto;
    }

   
    public Carta actualizarCarta(Administrador admin, Long id, String nombre, BigDecimal precioBaseUSD, String rareza) {
        Producto existente = buscarProducto(id);
        if (!(existente instanceof Carta)) {
            throw new IllegalArgumentException("El producto " + id + " no es una carta");
        }
        Carta nueva = new Carta(id, nombre, precioBaseUSD, leerRareza(rareza));
        nueva.setDisponible(existente.isDisponible());
        admin.gestionarProducto(nueva, Sistema.datos.getInventario());
        Sistema.guardar();
        return nueva;
    }

    public ProductoSellado actualizarSellado(Administrador admin, Long id, String nombre, BigDecimal precioBaseUSD,
                                             String tipo, String descripcion) {
        Producto existente = buscarProducto(id);
        if (!(existente instanceof ProductoSellado)) {
            throw new IllegalArgumentException("El producto " + id + " no es un producto sellado");
        }
        ProductoSellado nuevo = new ProductoSellado(id, nombre, precioBaseUSD, tipo, descripcion);
        nuevo.setDisponible(existente.isDisponible());
        admin.gestionarProducto(nuevo, Sistema.datos.getInventario());
        Sistema.guardar();
        return nuevo;
    }

    public Accesorio actualizarAccesorio(Administrador admin, Long id, String nombre, BigDecimal precioBaseUSD,
                                         String tipoAccesorio, String marca) {
        Producto existente = buscarProducto(id);
        if (!(existente instanceof Accesorio)) {
            throw new IllegalArgumentException("El producto " + id + " no es un accesorio");
        }
        Accesorio nuevo = new Accesorio(id, nombre, precioBaseUSD, tipoAccesorio, marca);
        nuevo.setDisponible(existente.isDisponible());
        admin.gestionarProducto(nuevo, Sistema.datos.getInventario());
        Sistema.guardar();
        return nuevo;
    }

    
    public void eliminarProducto(Long id) {
        Producto producto = buscarProducto(id);
        if (producto instanceof Booster) {
            throw new IllegalStateException("Los boosters no se pueden eliminar");
        }
        Sistema.datos.getInventario().getProductos().remove(producto);
        Sistema.guardar();
    }

   
    private Rareza leerRareza(String texto) {
        if (texto != null) {
            for (Rareza r : Rareza.values()) {
                if (r.getCodigo().equalsIgnoreCase(texto) || r.name().equalsIgnoreCase(texto)) {
                    return r;
                }
            }
        }
        throw new IllegalArgumentException("Rareza inválida. Use N, UC, R, SR o UR");
    }
}
