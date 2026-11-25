package com.example.Controladores;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import com.example.Modelo.Accesorio;
import com.example.Modelo.Administrador;
import com.example.Modelo.Booster;
import com.example.Modelo.Carta;
import com.example.Modelo.Producto;
import com.example.Modelo.ProductoSellado;
import com.example.Modelo.Rareza;
import com.example.Modelo.Tienda;
import com.example.Persistencia.RepositorioJSON;

public class ProductoControladores {
    private final Tienda tienda;
    private final RepositorioJSON repositorio;

    public ProductoControladores(Tienda tienda, RepositorioJSON repositorio) {
        this.tienda = tienda;
        this.repositorio = repositorio;
    }

    public Carta crearCarta(Administrador admin, String nombre, BigDecimal precioBaseUSD, String rareza)
            throws IOException {
        validarAdministrador(admin);
            Carta carta = new Carta(tienda.nuevoId(),
                    nombre, precioBaseUSD, leerRareza(rareza));
            admin.gestionarProducto(carta, tienda.getProductos());
            guardar();
        return carta;
    }

    public ProductoSellado crearSellado(Administrador admin, String nombre, BigDecimal precioBaseUSD,
                                        String tipo, String descripcion) throws IOException {
        validarAdministrador(admin);
        ProductoSellado sellado = new ProductoSellado(tienda.nuevoId(),
                nombre, precioBaseUSD, tipo, descripcion);
        admin.gestionarProducto(sellado, tienda.getProductos());
        guardar();
        return sellado;
    }

    public Accesorio crearAccesorio(Administrador admin, String nombre, BigDecimal precioBaseUSD,
                                    String tipoAccesorio, String marca) throws IOException {
        validarAdministrador(admin);
        Accesorio accesorio = new Accesorio(tienda.nuevoId(),
                nombre, precioBaseUSD, tipoAccesorio, marca);
        admin.gestionarProducto(accesorio, tienda.getProductos());
        guardar();
        return accesorio;
    }

    public Booster crearBooster(Administrador admin, String nombre, BigDecimal precioBaseUSD) throws IOException {
        validarAdministrador(admin);
        Booster booster = admin.crearBooster(tienda.nuevoId(), nombre, precioBaseUSD, tienda.getProductos());
        guardar();
        return booster;
    }

    public List<Producto> listarProductos() {
        List<Producto> disponibles = new java.util.ArrayList<>();
        for (Producto producto : tienda.getProductos()) {
            if (producto.isDisponible()) {
                disponibles.add(producto);
            }
        }
        return disponibles;
    }

    public Producto buscarProducto(Long id) {
        Producto producto = buscarProductoEnLista(id);
        if (producto == null) {
            throw new NoEncontradoException("El producto no existe");
        }
        return producto;
    }

    public Carta actualizarCarta(Administrador admin, Long id, String nombre, BigDecimal precioBaseUSD,
                                String rareza) throws IOException {
        validarAdministrador(admin);
        Producto existente = buscarProducto(id);
        if (!(existente instanceof Carta)) {
            throw new IllegalArgumentException("El producto " + id + " no es una carta");
        }
        Carta nueva = new Carta(id, nombre, precioBaseUSD, leerRareza(rareza));
        nueva.setDisponible(existente.isDisponible());
        admin.gestionarProducto(nueva, tienda.getProductos());
        guardar();
        return nueva;
    }

    public ProductoSellado actualizarSellado(Administrador admin, Long id, String nombre,
                                              BigDecimal precioBaseUSD, String tipo,
                                              String descripcion) throws IOException {
        validarAdministrador(admin);
        Producto existente = buscarProducto(id);
        if (!(existente instanceof ProductoSellado)) {
            throw new IllegalArgumentException("El producto " + id + " no es un producto sellado");
        }
        ProductoSellado nuevo = new ProductoSellado(id, nombre, precioBaseUSD, tipo, descripcion);
        nuevo.setDisponible(existente.isDisponible());
        admin.gestionarProducto(nuevo, tienda.getProductos());
        guardar();
        return nuevo;
    }

    public Accesorio actualizarAccesorio(Administrador admin, Long id, String nombre,
                                         BigDecimal precioBaseUSD, String tipoAccesorio,
                                         String marca) throws IOException {
        validarAdministrador(admin);
        Producto existente = buscarProducto(id);
        if (!(existente instanceof Accesorio)) {
            throw new IllegalArgumentException("El producto " + id + " no es un accesorio");
        }
        Accesorio nuevo = new Accesorio(id, nombre, precioBaseUSD, tipoAccesorio, marca);
        nuevo.setDisponible(existente.isDisponible());
        admin.gestionarProducto(nuevo, tienda.getProductos());
        guardar();
        return nuevo;
    }

    public void eliminarProducto(Administrador admin, Long id) throws IOException {
        validarAdministrador(admin);
        Producto producto = buscarProducto(id);
        if (producto instanceof Booster) {
            throw new IllegalStateException("Los boosters no se pueden eliminar");
        }
        tienda.getProductos().remove(producto);
        guardar();
    }

    private void validarAdministrador(Administrador admin) {
        if (admin == null || admin.getRol() != com.example.Modelo.Rol.ADMIN
                || !tienda.getUsuarios().contains(admin)) {
            throw new SecurityException("Se requiere un administrador registrado");
        }
    }

    private Producto buscarProductoEnLista(Long id) {
        for (Producto producto : tienda.getProductos()) {
            if (producto.getId().equals(id)) {
                return producto;
            }
        }
        return null;
    }

    private void guardar() throws IOException {
        repositorio.guardar(tienda);
    }

    private Rareza leerRareza(String texto) {
        if (texto != null) {
            for (Rareza rareza : Rareza.values()) {
                if (rareza.getCodigo().equalsIgnoreCase(texto) || rareza.name().equalsIgnoreCase(texto)) {
                    return rareza;
                }
            }
        }
        throw new IllegalArgumentException("Rareza inválida. Use N, UC, R, SR o UR");
    }
}
