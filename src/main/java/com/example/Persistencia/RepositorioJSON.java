package com.example.Persistencia;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.Modelo.Accesorio;
import com.example.Modelo.Administrador;
import com.example.Modelo.Booster;
import com.example.Modelo.Carta;
import com.example.Modelo.Cliente;
import com.example.Modelo.Compra;
import com.example.Modelo.Detalle;
import com.example.Modelo.Producto;
import com.example.Modelo.ProductoSellado;
import com.example.Modelo.Rareza;
import com.example.Modelo.Rol;
import com.example.Modelo.Tienda;
import com.example.Modelo.Torneo;
import com.example.Modelo.Usuario;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

public class RepositorioJSON {
    private final Path archivo;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public RepositorioJSON(Path archivo) {
        if (archivo == null) {
            throw new IllegalArgumentException("La ruta del archivo es obligatoria");
        }
        this.archivo = archivo;
    }

    public Tienda cargar() throws IOException {
        Tienda tienda = new Tienda();
        if (!Files.exists(archivo)) {
            return tienda;
        }

        Snapshot snapshot;
        try {
            snapshot = gson.fromJson(Files.readString(archivo), Snapshot.class);
        } catch (JsonParseException ex) {
            throw new IOException("El archivo JSON de datos no es válido: " + archivo, ex);
        }
        if (snapshot == null) {
            throw new IOException("El archivo JSON de datos está vacío: " + archivo);
        }
        DatosRestaurados datos = restaurar(snapshot);
        tienda.getProductos().addAll(datos.productos);
        tienda.getUsuarios().addAll(datos.usuarios);
        tienda.getTorneos().addAll(datos.torneos);
        return tienda;
    }

    public void guardar(Tienda tienda) throws IOException {
        if (tienda == null) {
            throw new IllegalArgumentException("La tienda es obligatoria");
        }
        Snapshot snapshot = crearSnapshot(tienda.getProductos(), tienda.getUsuarios(), tienda.getTorneos());
        Path rutaAbsoluta = archivo.toAbsolutePath();
        Path carpeta = rutaAbsoluta.getParent();
        if (carpeta != null) {
            Files.createDirectories(carpeta);
        }

        Path temporal = rutaAbsoluta.resolveSibling(rutaAbsoluta.getFileName() + ".tmp");
        Files.writeString(temporal, gson.toJson(snapshot));
        try {
            Files.move(temporal, rutaAbsoluta, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(temporal, rutaAbsoluta, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private Snapshot crearSnapshot(List<Producto> productos, List<Usuario> usuarios, List<Torneo> torneos) {
        Snapshot snapshot = new Snapshot();

        for (Producto producto : productos) {
            snapshot.productos.add(crearProductoDTO(producto));
        }
        for (Usuario usuario : usuarios) {
            snapshot.usuarios.add(crearUsuarioDTO(usuario));
        }
        for (Torneo torneo : torneos) {
            TorneoDTO dto = new TorneoDTO();
            dto.id = torneo.getId();
            dto.nombre = torneo.getNombre();
            dto.fechaLimiteInscripcion = torneo.getFechaLimiteInscripcion().getTime();
            dto.cupoMaximo = torneo.getCupoMaximo();
            dto.administradorId = torneo.getAdministradorId();
            for (Usuario participante : torneo.obtenerParticipantes()) {
                dto.participantes.add(participante.getId());
            }
            snapshot.torneos.add(dto);
        }
        return snapshot;
    }

    private ProductoDTO crearProductoDTO(Producto producto) {
        ProductoDTO dto = new ProductoDTO();
        dto.id = producto.getId();
        dto.nombre = producto.getNombre();
        dto.precioBaseUSD = producto.getPrecioBaseUSD();
        dto.disponible = producto.isDisponible();

        if (producto instanceof Carta) {
            dto.tipo = "CARTA";
            dto.rareza = ((Carta) producto).getRareza().name();
        } else if (producto instanceof Booster) {
            Booster booster = (Booster) producto;
            dto.tipo = "BOOSTER";
            dto.compradorId = booster.getCompradorId();
            dto.vendido = booster.isVendido();
            dto.abierto = booster.isAbierto();
            for (Carta carta : booster.getContenido()) {
                dto.contenidoIds.add(carta.getId());
            }
        } else if (producto instanceof ProductoSellado) {
            ProductoSellado sellado = (ProductoSellado) producto;
            dto.tipo = "SELLADO";
            dto.tipoProducto = sellado.getTipo();
            dto.descripcion = sellado.getDescripcion();
        } else if (producto instanceof Accesorio) {
            Accesorio accesorio = (Accesorio) producto;
            dto.tipo = "ACCESORIO";
            dto.tipoProducto = accesorio.getTipoAccesorio();
            dto.marca = accesorio.getMarca();
        } else {
            throw new IllegalStateException("Tipo de producto no soportado: " + producto.getClass().getName());
        }
        return dto;
    }

    private UsuarioDTO crearUsuarioDTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.id = usuario.getId();
        dto.username = usuario.getUsername();
        dto.password = usuario.getPassword();
        dto.rol = usuario.getRol().name();

        if (usuario instanceof Cliente) {
            Cliente cliente = (Cliente) usuario;
            for (Detalle detalle : cliente.getInventarioPersonal()) {
                dto.inventarioPersonal.add(crearDetalleDTO(detalle));
            }
            for (Compra compra : cliente.getHistorialCompras()) {
                CompraDTO compraDTO = new CompraDTO();
                compraDTO.id = compra.getId();
                compraDTO.fecha = compra.getFecha().getTime();
                for (Detalle detalle : compra.getDetalles()) {
                    compraDTO.detalles.add(crearDetalleDTO(detalle));
                }
                dto.compras.add(compraDTO);
            }
            for (Torneo torneo : cliente.getTorneosInscritos()) {
                dto.torneosInscritos.add(torneo.getId());
            }
        }
        return dto;
    }

    private DetalleDTO crearDetalleDTO(Detalle detalle) {
        DetalleDTO dto = new DetalleDTO();
        dto.productoId = detalle.getProducto().getId();
        dto.cantidad = detalle.getCantidad();
        dto.subtotal = detalle.getSubtotal();
        return dto;
    }

    private DatosRestaurados restaurar(Snapshot snapshot) throws IOException {
        List<Producto> productos = new ArrayList<>();
        Map<Long, Producto> productosPorId = new HashMap<>();
        for (ProductoDTO dto : snapshot.productos) {
            Producto producto = restaurarProducto(dto);
            productos.add(producto);
            producto.setDisponible(dto.disponible);
            productosPorId.put(producto.getId(), producto);
        }

        Map<Long, Usuario> usuariosPorId = new HashMap<>();
        List<Usuario> usuarios = new ArrayList<>();
        Map<Long, UsuarioDTO> usuariosDTO = new HashMap<>();
        for (UsuarioDTO dto : snapshot.usuarios) {
            Usuario usuario;
            if (Rol.ADMIN.name().equals(dto.rol)) {
                usuario = new Administrador(dto.id, dto.username, dto.password);
            } else if (Rol.CLIENTE.name().equals(dto.rol)) {
                usuario = new Cliente(dto.id, dto.username, dto.password);
            } else {
                throw new IOException("Rol de usuario desconocido en el archivo JSON: " + dto.rol);
            }
            usuarios.add(usuario);
            usuariosPorId.put(usuario.getId(), usuario);
            usuariosDTO.put(usuario.getId(), dto);
        }

        restaurarBoosters(snapshot.productos, productosPorId);

        Map<Long, Torneo> torneosPorId = new HashMap<>();
        List<Torneo> torneos = new ArrayList<>();
        for (TorneoDTO dto : snapshot.torneos) {
            Torneo torneo = new Torneo(dto.id, dto.nombre, new Date(dto.fechaLimiteInscripcion),
                    dto.cupoMaximo, dto.administradorId);
            List<Usuario> participantes = new ArrayList<>();
            for (Long usuarioId : dto.participantes) {
                Usuario participante = usuariosPorId.get(usuarioId);
                if (participante == null) {
                    throw new IOException("Participante de torneo inexistente: " + usuarioId);
                }
                participantes.add(participante);
            }
            torneo.restaurarParticipantes(participantes);
            torneos.add(torneo);
            torneosPorId.put(torneo.getId(), torneo);
        }

        for (Usuario usuario : usuarios) {
            if (!(usuario instanceof Cliente)) {
                continue;
            }
            Cliente cliente = (Cliente) usuario;
            UsuarioDTO dto = usuariosDTO.get(cliente.getId());
            List<Detalle> inventarioPersonal = restaurarDetalles(dto.inventarioPersonal, productosPorId);
            cliente.restaurarInventarioPersonal(inventarioPersonal);

            List<Compra> compras = new ArrayList<>();
            for (CompraDTO compraDTO : dto.compras) {
                List<Detalle> detalles = restaurarDetalles(compraDTO.detalles, productosPorId);
                compras.add(new Compra(compraDTO.id, new Date(compraDTO.fecha), cliente, detalles));
            }
            cliente.restaurarHistorialCompras(compras);

            List<Torneo> torneosCliente = new ArrayList<>();
            for (Long torneoId : dto.torneosInscritos) {
                Torneo torneo = torneosPorId.get(torneoId);
                if (torneo == null) {
                    throw new IOException("Torneo inscrito inexistente: " + torneoId);
                }
                torneosCliente.add(torneo);
            }
            cliente.restaurarTorneosInscritos(torneosCliente);
        }

        return new DatosRestaurados(productos, usuarios, torneos);
    }

    private Producto restaurarProducto(ProductoDTO dto) throws IOException {
        Producto producto;
        try {
            switch (dto.tipo) {
                case "CARTA":
                    producto = new Carta(dto.id, dto.nombre, dto.precioBaseUSD, Rareza.valueOf(dto.rareza));
                    break;
                case "BOOSTER":
                    producto = new Booster(dto.id, dto.nombre, dto.precioBaseUSD);
                    break;
                case "SELLADO":
                    producto = new ProductoSellado(dto.id, dto.nombre, dto.precioBaseUSD,
                            dto.tipoProducto, dto.descripcion);
                    break;
                case "ACCESORIO":
                    producto = new Accesorio(dto.id, dto.nombre, dto.precioBaseUSD,
                            dto.tipoProducto, dto.marca);
                    break;
                default:
                    throw new IOException("Tipo de producto desconocido: " + dto.tipo);
            }
        } catch (IllegalArgumentException ex) {
            throw new IOException("Producto inválido en el archivo JSON: " + dto.id, ex);
        }
        return producto;
    }

    private void restaurarBoosters(List<ProductoDTO> productosDTO, Map<Long, Producto> productosPorId)
            throws IOException {
        for (ProductoDTO dto : productosDTO) {
            if (!"BOOSTER".equals(dto.tipo)) {
                continue;
            }
            List<Carta> contenido = new ArrayList<>();
            for (Long cartaId : dto.contenidoIds) {
                Producto carta = productosPorId.get(cartaId);
                if (!(carta instanceof Carta)) {
                    throw new IOException("Carta del booster inexistente: " + cartaId);
                }
                contenido.add((Carta) carta);
            }
            ((Booster) productosPorId.get(dto.id)).restaurarEstado(
                    contenido, dto.compradorId, dto.vendido, dto.abierto);
        }
    }

    private List<Detalle> restaurarDetalles(List<DetalleDTO> datos, Map<Long, Producto> productosPorId)
            throws IOException {
        List<Detalle> detalles = new ArrayList<>();
        for (DetalleDTO dto : datos) {
            Producto producto = productosPorId.get(dto.productoId);
            if (producto == null) {
                throw new IOException("Producto de detalle inexistente: " + dto.productoId);
            }
            detalles.add(Detalle.restaurar(producto, dto.cantidad, dto.subtotal));
        }
        return detalles;
    }

    private static class Snapshot {
        List<ProductoDTO> productos = new ArrayList<>();
        List<UsuarioDTO> usuarios = new ArrayList<>();
        List<TorneoDTO> torneos = new ArrayList<>();
    }

    private static class DatosRestaurados {
        final List<Producto> productos;
        final List<Usuario> usuarios;
        final List<Torneo> torneos;

        DatosRestaurados(List<Producto> productos, List<Usuario> usuarios, List<Torneo> torneos) {
            this.productos = productos;
            this.usuarios = usuarios;
            this.torneos = torneos;
        }
    }

    private static class ProductoDTO {
        String tipo;
        Long id;
        String nombre;
        BigDecimal precioBaseUSD;
        boolean disponible;
        String rareza;
        String tipoProducto;
        String descripcion;
        String marca;
        List<Long> contenidoIds = new ArrayList<>();
        Long compradorId;
        boolean vendido;
        boolean abierto;
    }

    private static class UsuarioDTO {
        Long id;
        String username;
        String password;
        String rol;
        List<DetalleDTO> inventarioPersonal = new ArrayList<>();
        List<CompraDTO> compras = new ArrayList<>();
        List<Long> torneosInscritos = new ArrayList<>();
    }

    private static class DetalleDTO {
        Long productoId;
        int cantidad;
        BigDecimal subtotal;
    }

    private static class CompraDTO {
        Long id;
        long fecha;
        List<DetalleDTO> detalles = new ArrayList<>();
    }

    private static class TorneoDTO {
        Long id;
        String nombre;
        long fechaLimiteInscripcion;
        Integer cupoMaximo;
        Long administradorId;
        List<Long> participantes = new ArrayList<>();
    }
}
