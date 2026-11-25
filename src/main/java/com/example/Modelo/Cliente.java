package com.example.Modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Cliente extends Usuario {
    
private List<Detalle> inventarioPersonal = new ArrayList<>();
    private List<Compra> historialCompras = new ArrayList<>();
    private List<Torneo> torneosInscritos = new ArrayList<>();

    public Cliente(Long id, String username, String password) {
        super(id, username, password, Rol.CLIENTE);
    }

    public List<Producto> consultarCatalogo(List<Producto> productos) {
        List<Producto> disponibles = new ArrayList<>();
        for (Producto producto : productos) {
            if (producto.isDisponible()) {
                disponibles.add(producto);
            }
        }
        return disponibles;
    }

    // ---------- Inventario personal (antes de pagar) ----------
    /** El subtotal se calcula aquí, al crear el Detalle, con la tasa del momento. */
    public void agregarAlInventario(Producto producto, BigDecimal tasaCambio) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto es obligatorio");
        }
        if (!producto.isDisponible()) {
            throw new IllegalStateException("El producto '" + producto.getNombre() + "' no está disponible");
        }
        for (Detalle d : inventarioPersonal) {
            if (d.getProducto().getId().equals(producto.getId())) {
                throw new IllegalStateException("El producto ya está en tu inventario personal");
            }
        }
        inventarioPersonal.add(new Detalle(producto, 1, tasaCambio));
    }

    public void quitarDelInventario(Producto producto) {
        for (int i = 0; i < inventarioPersonal.size(); i++) {
            if (inventarioPersonal.get(i).getProducto().getId().equals(producto.getId())) {
                inventarioPersonal.remove(i);
                return;
            }
        }
        throw new IllegalArgumentException("El producto no está en tu inventario personal");
    }

    /** Lo que va a pagar: la suma de los subtotales ya calculados. */
    public BigDecimal verSubtotal() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (Detalle d : inventarioPersonal) {
            subtotal = subtotal.add(d.getSubtotal());
        }
        return subtotal;
    }

    // ---------- Pagar ----------
    /** Verifica disponibilidad, retira los productos de la tienda y genera la compra con sus detalles. */
    public Compra pagar(List<Producto> productosTienda, Long idCompra) {
        if (inventarioPersonal.isEmpty()) {
            throw new IllegalArgumentException("No hay productos en el inventario personal");
        }
        List<Producto> productosComprados = new ArrayList<>();
        for (Detalle d : inventarioPersonal) {
            Producto productoRegistrado = buscarProducto(d.getProducto().getId(), productosTienda);
            if (productoRegistrado == null || !productoRegistrado.isDisponible()) {
                throw new IllegalStateException("El producto '" + d.getProducto().getNombre() + "' ya no está disponible");
            }
            if (productoRegistrado instanceof Booster && ((Booster) productoRegistrado).isVendido()) {
                throw new IllegalStateException("El booster ya fue vendido");
            }
            productosComprados.add(productoRegistrado);
        }
        Compra compra = idCompra == null ? new Compra(this) : new Compra(idCompra, this);
        for (int i = 0; i < inventarioPersonal.size(); i++) {
            Detalle d = inventarioPersonal.get(i);
            Producto p = productosComprados.get(i);
            compra.agregarDetalle(d);
            if (p instanceof Booster) {
                ((Booster) p).marcarVendido(getId());
            }
            p.setDisponible(false);
        }
        historialCompras.add(compra);
        inventarioPersonal.clear();
        return compra;
    }

    private Producto buscarProducto(Long id, List<Producto> productos) {
        for (Producto producto : productos) {
            if (producto.getId().equals(id)) {
                return producto;
            }
        }
        return null;
    }

    /** Solo el dueño puede abrirlo, y una sola vez (lo valida Booster). */
    public List<Carta> abrirBooster(Booster booster) {
        return booster.abrirBooster(getId());
    }

    /** El torneo valida plazo, cupo y duplicado; si hay error, no se agrega nada. */
    public boolean inscribirseEnTorneo(Torneo torneo) {
        boolean inscrito = torneo.inscribirCliente(this);
        torneosInscritos.add(torneo);
        return inscrito;
    }

    public List<Compra> consultarHistorial() {
        return historialCompras;
    }

    public List<Detalle> getInventarioPersonal() { return inventarioPersonal; }
    public List<Compra> getHistorialCompras() { return historialCompras; }
    public List<Torneo> getTorneosInscritos() { return torneosInscritos; }

    public void restaurarInventarioPersonal(List<Detalle> detalles) {
        inventarioPersonal.clear();
        inventarioPersonal.addAll(detalles);
    }

    public void restaurarHistorialCompras(List<Compra> compras) {
        historialCompras.clear();
        historialCompras.addAll(compras);
    }

    public void restaurarTorneosInscritos(List<Torneo> torneos) {
        torneosInscritos.clear();
        torneosInscritos.addAll(torneos);
    }
}

