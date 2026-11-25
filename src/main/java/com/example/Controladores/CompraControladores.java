package com.example.Controladores;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import com.example.Modelo.Booster;
import com.example.Modelo.Carta;
import com.example.Modelo.Cliente;
import com.example.Modelo.Compra;
import com.example.Modelo.Producto;
import com.example.Modelo.Tienda;
import com.example.Persistencia.RepositorioJSON;

public class CompraControladores {
    private final Tienda tienda;
    private final RepositorioJSON repositorio;

    public CompraControladores(Tienda tienda, RepositorioJSON repositorio) {
        this.tienda = tienda;
        this.repositorio = repositorio;
    }

    public void agregarAlInventario(Cliente cliente, Long productoId, BigDecimal tasaCambio) throws IOException {
        validarCliente(cliente);
        Producto producto = buscarProducto(productoId);
        if (producto == null) {
            throw new NoEncontradoException("El producto no existe");
        }
        cliente.agregarAlInventario(producto, tasaCambio);
        guardar();
    }

    public void quitarDelInventario(Cliente cliente, Long productoId) throws IOException {
        validarCliente(cliente);
        Producto producto = buscarProducto(productoId);
        if (producto == null) {
            throw new NoEncontradoException("El producto no existe");
        }
        cliente.quitarDelInventario(producto);
        guardar();
    }

    public BigDecimal verSubtotal(Cliente cliente) {
        validarCliente(cliente);
        return cliente.verSubtotal();
    }

    public Compra pagar(Cliente cliente) throws IOException {
        validarCliente(cliente);
        Compra compra = cliente.pagar(tienda.getProductos(), tienda.nuevoId());
        guardar();
        return compra;
    }

    public List<Compra> consultarHistorial(Cliente cliente) {
        validarCliente(cliente);
        return cliente.consultarHistorial();
    }

    public List<Carta> abrirBooster(Cliente cliente, Long productoId) throws IOException {
        validarCliente(cliente);
        Producto producto = buscarProducto(productoId);
        if (!(producto instanceof Booster)) {
            throw new NoEncontradoException("El booster no existe");
        }
        List<Carta> cartas = cliente.abrirBooster((Booster) producto);
        guardar();
        return cartas;
    }

    private void validarCliente(Cliente cliente) {
        if (cliente == null || !tienda.getUsuarios().contains(cliente)) {
            throw new SecurityException("El cliente debe estar registrado");
        }
    }

    private Producto buscarProducto(Long id) {
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
}
