package com.example;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.Controladores.CompraControladores;
import com.example.Controladores.NoEncontradoException;
import com.example.Controladores.ProductoControladores;
import com.example.Controladores.TorneoControladores;
import com.example.Controladores.UsuarioControladores;
import com.example.Integraciones.TasaCambioService;
import com.example.Modelo.Accesorio;
import com.example.Modelo.Administrador;
import com.example.Modelo.Booster;
import com.example.Modelo.Carta;
import com.example.Modelo.Cliente;
import com.example.Modelo.Compra;
import com.example.Modelo.Detalle;
import com.example.Modelo.Producto;
import com.example.Modelo.ProductoSellado;
import com.example.Modelo.Tienda;
import com.example.Modelo.Torneo;
import com.example.Modelo.Usuario;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import spark.Request;
import spark.Response;
import static spark.Spark.delete;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.notFound;
import static spark.Spark.post;
import static spark.Spark.put;

public class ApiRest {
    private final Tienda tienda;
    private final ProductoControladores productos;
    private final UsuarioControladores usuarios;
    private final CompraControladores compras;
    private final TorneoControladores torneos;
    private final TasaCambioService tasaCambio;
    private final Gson gson;

    public ApiRest(Tienda tienda, com.example.Persistencia.RepositorioJSON repositorio,
                   TasaCambioService tasaCambio, Gson gson) {
        this.tienda = tienda;
        this.productos = new ProductoControladores(tienda, repositorio);
        this.usuarios = new UsuarioControladores(tienda, repositorio);
        this.compras = new CompraControladores(tienda, repositorio);
        this.torneos = new TorneoControladores(tienda, repositorio);
        this.tasaCambio = tasaCambio;
        this.gson = gson;
    }

    public void registrarRutas() {
        get("/health", (request, response) -> {
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("estado", "ACTIVA");
            return responder(response, 200, json);
        });

        get("/tasa", (request, response) -> {
            TasaCambioService.ResultadoTasa resultado = tasaCambio.obtenerTasaUsdCop();
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("tasaUsdCop", resultado.getTasa());
            json.put("fuenteTasa", resultado.getFuenteTasa());
            return responder(response, 200, json);
        });

        get("/estado", (request, response) -> {
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("estado", "ACTIVA");
            json.put("productos", tienda.getProductos().size());
            json.put("usuarios", tienda.getUsuarios().size());
            json.put("torneos", tienda.getTorneos().size());
            return responder(response, 200, json);
        });

        get("/auth/me", (request, response) -> {
            Usuario usuario = autenticar(request);
            return responder(response, 200, usuarioJson(usuario));
        });

        get("/productos", (request, response) -> {
            autenticar(request);
            TasaCambioService.ResultadoTasa tasa = tasaCambio.obtenerTasaUsdCop();
            List<Map<String, Object>> resultado = new ArrayList<>();
            for (Producto producto : productos.listarProductos()) {
                resultado.add(productoJson(producto, tasa, null));
            }
            return responder(response, 200, resultado);
        });

        get("/productos/:id", (request, response) -> {
            Usuario usuario = autenticar(request);
            TasaCambioService.ResultadoTasa tasa = tasaCambio.obtenerTasaUsdCop();
            Producto producto = productos.buscarProducto(id(request.params(":id")));
            return responder(response, 200, productoJson(producto, tasa,
                    usuario instanceof Cliente ? (Cliente) usuario : null));
        });

        post("/productos/cartas", (request, response) -> {
            Administrador admin = administrador(request);
            ProductoRequest datos = body(request, ProductoRequest.class);
            Carta carta = productos.crearCarta(admin, datos.nombre, datos.precioBaseUSD, datos.rareza);
            return responder(response, 201,
                    productoJson(carta, tasaCambio.obtenerTasaUsdCop(), null));
        });

        post("/productos/sellados", (request, response) -> {
            Administrador admin = administrador(request);
            ProductoRequest datos = body(request, ProductoRequest.class);
            ProductoSellado producto = productos.crearSellado(admin, datos.nombre,
                    datos.precioBaseUSD, datos.tipo, datos.descripcion);
            return responder(response, 201,
                    productoJson(producto, tasaCambio.obtenerTasaUsdCop(), null));
        });

        post("/productos/accesorios", (request, response) -> {
            Administrador admin = administrador(request);
            ProductoRequest datos = body(request, ProductoRequest.class);
            Accesorio producto = productos.crearAccesorio(admin, datos.nombre,
                    datos.precioBaseUSD, datos.tipoAccesorio, datos.marca);
            return responder(response, 201,
                    productoJson(producto, tasaCambio.obtenerTasaUsdCop(), null));
        });

        post("/productos/boosters", (request, response) -> {
            Administrador admin = administrador(request);
            ProductoRequest datos = body(request, ProductoRequest.class);
            Booster producto = productos.crearBooster(admin, datos.nombre, datos.precioBaseUSD);
            return responder(response, 201,
                    productoJson(producto, tasaCambio.obtenerTasaUsdCop(), null));
        });

        put("/productos/cartas/:id", (request, response) -> {
            Administrador admin = administrador(request);
            ProductoRequest datos = body(request, ProductoRequest.class);
            Carta producto = productos.actualizarCarta(admin, id(request.params(":id")),
                    datos.nombre, datos.precioBaseUSD, datos.rareza);
            return responder(response, 200,
                    productoJson(producto, tasaCambio.obtenerTasaUsdCop(), null));
        });

        put("/productos/sellados/:id", (request, response) -> {
            Administrador admin = administrador(request);
            ProductoRequest datos = body(request, ProductoRequest.class);
            ProductoSellado producto = productos.actualizarSellado(admin, id(request.params(":id")),
                    datos.nombre, datos.precioBaseUSD, datos.tipo, datos.descripcion);
            return responder(response, 200,
                    productoJson(producto, tasaCambio.obtenerTasaUsdCop(), null));
        });

        put("/productos/accesorios/:id", (request, response) -> {
            Administrador admin = administrador(request);
            ProductoRequest datos = body(request, ProductoRequest.class);
            Accesorio producto = productos.actualizarAccesorio(admin, id(request.params(":id")),
                    datos.nombre, datos.precioBaseUSD, datos.tipoAccesorio, datos.marca);
            return responder(response, 200,
                    productoJson(producto, tasaCambio.obtenerTasaUsdCop(), null));
        });

        delete("/productos/:id", (request, response) -> {
            productos.eliminarProducto(administrador(request), id(request.params(":id")));
            response.status(204);
            return "";
        });

        post("/clientes", (request, response) -> {
            ClienteRequest datos = body(request, ClienteRequest.class);
            Cliente cliente = usuarios.crearCliente(administrador(request), datos.username, datos.password);
            return responder(response, 201, usuarioJson(cliente));
        });

        get("/clientes", (request, response) -> {
            administrador(request);
            List<Map<String, Object>> lista = new ArrayList<>();
            for (Cliente cliente : usuarios.listarClientes()) {
                lista.add(usuarioJson(cliente));
            }
            return responder(response, 200, lista);
        });

        get("/clientes/:id", (request, response) -> responder(response, 200,
                usuarioJson(usuarios.buscarCliente(administrador(request), id(request.params(":id"))))));

        put("/clientes/:id", (request, response) -> {
            ClienteRequest datos = body(request, ClienteRequest.class);
            Cliente cliente = usuarios.actualizarCliente(administrador(request),
                    id(request.params(":id")), datos.username, datos.password);
            return responder(response, 200, usuarioJson(cliente));
        });

        delete("/clientes/:id", (request, response) -> {
            usuarios.eliminarCliente(administrador(request), id(request.params(":id")));
            response.status(204);
            return "";
        });

        get("/carrito", (request, response) -> {
            Cliente cliente = cliente(request);
            TasaCambioService.ResultadoTasa tasa = tasaCambio.obtenerTasaUsdCop();
            List<Map<String, Object>> items = new ArrayList<>();
            for (Detalle detalle : cliente.getInventarioPersonal()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("producto", productoJson(detalle.getProducto(), tasa, cliente));
                item.put("cantidad", detalle.getCantidad());
                item.put("subtotalCOP", detalle.getSubtotal());
                items.add(item);
            }
            Map<String, Object> resultado = new LinkedHashMap<>();
            resultado.put("items", items);
            resultado.put("subtotalCOP", compras.verSubtotal(cliente));
            resultado.put("fuenteTasa", tasa.getFuenteTasa());
            return responder(response, 200, resultado);
        });

        post("/carrito", (request, response) -> {
            Cliente cliente = cliente(request);
            CarritoRequest datos = body(request, CarritoRequest.class);
            TasaCambioService.ResultadoTasa tasa = tasaCambio.obtenerTasaUsdCop();
            compras.agregarAlInventario(cliente, requerido(datos.productoId, "productoId"), tasa.getTasa());
            Map<String, Object> resultado = new LinkedHashMap<>();
            resultado.put("mensaje", "Producto agregado al carrito");
            resultado.put("subtotalCOP", compras.verSubtotal(cliente));
            resultado.put("fuenteTasa", tasa.getFuenteTasa());
            return responder(response, 201, resultado);
        });

        delete("/carrito/:productoId", (request, response) -> {
            compras.quitarDelInventario(cliente(request), id(request.params(":productoId")));
            response.status(204);
            return "";
        });

        post("/compras", (request, response) -> {
            Cliente cliente = cliente(request);
            Compra compra = compras.pagar(cliente);
            return responder(response, 201, compraJson(compra, cliente));
        });

        get("/compras", (request, response) -> {
            Cliente cliente = cliente(request);
            List<Map<String, Object>> historial = new ArrayList<>();
            for (Compra compra : compras.consultarHistorial(cliente)) {
                historial.add(compraJson(compra, cliente));
            }
            return responder(response, 200, historial);
        });

        post("/boosters/:id/abrir", (request, response) -> {
            Cliente cliente = cliente(request);
            List<Carta> cartas = compras.abrirBooster(cliente, id(request.params(":id")));
            List<Map<String, Object>> resultado = new ArrayList<>();
            TasaCambioService.ResultadoTasa tasa = tasaCambio.obtenerTasaUsdCop();
            for (Carta carta : cartas) {
                resultado.add(productoJson(carta, tasa, cliente));
            }
            return responder(response, 200, resultado);
        });

        get("/torneos", (request, response) -> {
            autenticar(request);
            List<Map<String, Object>> lista = new ArrayList<>();
            for (Torneo torneo : torneos.listarTorneos()) {
                lista.add(torneoJson(torneo));
            }
            return responder(response, 200, lista);
        });

        get("/torneos/:id", (request, response) -> responder(response, 200,
                torneoJson(torneos.buscarTorneo(id(request.params(":id"))))));

        post("/torneos", (request, response) -> {
            TorneoRequest datos = body(request, TorneoRequest.class);
            Torneo torneo = torneos.crearTorneo(administrador(request), datos.nombre,
                    fechaLimite(datos.fechaLimiteInscripcion), datos.cupoMaximo);
            return responder(response, 201, torneoJson(torneo));
        });

        post("/torneos/:id/inscripciones", (request, response) -> {
            Torneo torneo = torneos.inscribirCliente(cliente(request), id(request.params(":id")));
            return responder(response, 201, torneoJson(torneo));
        });

        notFound((request, response) -> {
            response.type("application/json");
            response.status(404);
            return gson.toJson(Map.of("error", "La ruta solicitada no existe"));
        });

        exception(NoEncontradoException.class, (excepcion, request, response) ->
                error(response, 404, excepcion.getMessage()));
        exception(UnauthorizedException.class, (excepcion, request, response) -> {
            response.header("WWW-Authenticate", "Basic realm=\"Cards4Play\"");
            error(response, 401, excepcion.getMessage());
        });
        exception(SecurityException.class, (excepcion, request, response) ->
                error(response, 403, excepcion.getMessage()));
        exception(IllegalArgumentException.class, (excepcion, request, response) ->
                error(response, 400, excepcion.getMessage()));
        exception(IllegalStateException.class, (excepcion, request, response) ->
                error(response, 409, excepcion.getMessage()));
        exception(IOException.class, (excepcion, request, response) ->
                error(response, 500, "No fue posible guardar los datos"));
        exception(Exception.class, (excepcion, request, response) -> {
            excepcion.printStackTrace();
            error(response, 500, "Error interno del servidor");
        });
    }

    private Usuario autenticar(Request request) {
        String header = request.headers("Authorization");
        if (header == null || !header.startsWith("Basic ")) {
            throw new UnauthorizedException("Se requiere autenticación Basic");
        }
        try {
            String credenciales = new String(
                    Base64.getDecoder().decode(header.substring(6).trim()), StandardCharsets.UTF_8);
            String[] partes = credenciales.split(":", 2);
            if (partes.length != 2) {
                throw new UnauthorizedException("Credenciales inválidas");
            }
            for (Usuario usuario : tienda.getUsuarios()) {
                if (usuario.iniciarSesion(partes[0], partes[1])) {
                    return usuario;
                }
            }
        } catch (IllegalArgumentException excepcion) {
            throw new UnauthorizedException("Credenciales inválidas");
        }
        throw new UnauthorizedException("Usuario o contraseña incorrectos");
    }

    private Administrador administrador(Request request) {
        Usuario usuario = autenticar(request);
        if (!(usuario instanceof Administrador)) {
            throw new SecurityException("Se requiere el rol ADMIN");
        }
        return (Administrador) usuario;
    }

    private Cliente cliente(Request request) {
        Usuario usuario = autenticar(request);
        if (!(usuario instanceof Cliente)) {
            throw new SecurityException("Se requiere el rol CLIENTE");
        }
        return (Cliente) usuario;
    }

    private <T> T body(Request request, Class<T> tipo) {
        try {
            T datos = gson.fromJson(request.body(), tipo);
            if (datos == null) {
                throw new IllegalArgumentException("El cuerpo JSON es obligatorio");
            }
            return datos;
        } catch (JsonParseException excepcion) {
            throw new IllegalArgumentException("El cuerpo debe ser un JSON válido");
        }
    }

    private Long id(String texto) {
        try {
            return Long.valueOf(texto);
        } catch (NumberFormatException excepcion) {
            throw new IllegalArgumentException("El identificador debe ser numérico");
        }
    }

    private Long requerido(Long valor, String nombre) {
        if (valor == null) {
            throw new IllegalArgumentException("El campo " + nombre + " es obligatorio");
        }
        return valor;
    }

    private Date fechaLimite(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("fechaLimiteInscripcion es obligatoria (formato AAAA-MM-DD)");
        }
        try {
            return Date.from(LocalDate.parse(texto)
                    .atTime(LocalTime.MAX)
                    .atZone(ZoneId.systemDefault())
                    .toInstant());
        } catch (DateTimeParseException excepcion) {
            throw new IllegalArgumentException("La fecha debe tener formato AAAA-MM-DD");
        }
    }

    private Map<String, Object> productoJson(Producto producto,
            TasaCambioService.ResultadoTasa tasa, Cliente cliente) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("id", producto.getId());
        json.put("nombre", producto.getNombre());
        json.put("tipo", tipoProducto(producto));
        json.put("precioBaseUSD", producto.getPrecioBaseUSD());
        json.put("precioCOP", producto.calcularPrecioCOP(tasa.getTasa()));
        json.put("fuenteTasa", tasa.getFuenteTasa());
        json.put("disponible", producto.isDisponible());
        if (producto instanceof Carta) {
            json.put("rareza", ((Carta) producto).getRareza().name());
        } else if (producto instanceof ProductoSellado) {
            ProductoSellado sellado = (ProductoSellado) producto;
            json.put("tipoProducto", sellado.getTipo());
            json.put("descripcion", sellado.getDescripcion());
        } else if (producto instanceof Accesorio) {
            Accesorio accesorio = (Accesorio) producto;
            json.put("tipoAccesorio", accesorio.getTipoAccesorio());
            json.put("marca", accesorio.getMarca());
        } else if (producto instanceof Booster) {
            Booster booster = (Booster) producto;
            json.put("vendido", booster.isVendido());
            json.put("abierto", booster.isAbierto());
            if (cliente != null && booster.isAbierto()
                    && cliente.getId().equals(booster.getCompradorId())) {
                List<Map<String, Object>> contenido = new ArrayList<>();
                for (Carta carta : booster.getContenido()) {
                    Map<String, Object> cartaJson = new LinkedHashMap<>();
                    cartaJson.put("id", carta.getId());
                    cartaJson.put("nombre", carta.getNombre());
                    cartaJson.put("rareza", carta.getRareza().name());
                    contenido.add(cartaJson);
                }
                json.put("contenido", contenido);
            }
        }
        return json;
    }

    private String tipoProducto(Producto producto) {
        if (producto instanceof Carta) return "CARTA";
        if (producto instanceof Booster) return "BOOSTER";
        if (producto instanceof ProductoSellado) return "SELLADO";
        if (producto instanceof Accesorio) return "ACCESORIO";
        throw new IllegalStateException("Tipo de producto no soportado");
    }

    private Map<String, Object> usuarioJson(Usuario usuario) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("id", usuario.getId());
        json.put("username", usuario.getUsername());
        json.put("rol", usuario.getRol().name());
        return json;
    }

    private Map<String, Object> compraJson(Compra compra, Cliente cliente) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("id", compra.getId());
        json.put("fecha", compra.getFecha());
        json.put("cliente", usuarioJson(compra.getCliente()));
        List<Map<String, Object>> detalles = new ArrayList<>();
        for (Detalle detalle : compra.getDetalles()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("productoId", detalle.getProducto().getId());
            item.put("nombre", detalle.getProducto().getNombre());
            item.put("tipo", tipoProducto(detalle.getProducto()));
            item.put("cantidad", detalle.getCantidad());
            item.put("subtotalCOP", detalle.getSubtotal());
            if (detalle.getProducto() instanceof Booster) {
                Booster booster = (Booster) detalle.getProducto();
                if (booster.isAbierto() && cliente.getId().equals(booster.getCompradorId())) {
                    List<Map<String, Object>> cartas = new ArrayList<>();
                    for (Carta carta : booster.getContenido()) {
                        Map<String, Object> cartaJson = new LinkedHashMap<>();
                        cartaJson.put("id", carta.getId());
                        cartaJson.put("nombre", carta.getNombre());
                        cartaJson.put("rareza", carta.getRareza().name());
                        cartas.add(cartaJson);
                    }
                    item.put("cartasObtenidas", cartas);
                }
            }
            detalles.add(item);
        }
        json.put("detalles", detalles);
        json.put("totalPagadoCOP", compra.getTotalPagadoCOP());
        return json;
    }

    private Map<String, Object> torneoJson(Torneo torneo) {
        Map<String, Object> json = new LinkedHashMap<>();
        json.put("id", torneo.getId());
        json.put("nombre", torneo.getNombre());
        json.put("fechaLimiteInscripcion", torneo.getFechaLimiteInscripcion());
        json.put("cupoMaximo", torneo.getCupoMaximo());
        json.put("participantes", torneo.obtenerParticipantes().size());
        json.put("participantesIds", torneo.obtenerParticipantes().stream()
                .map(Usuario::getId).toArray());
        json.put("inscripcionesAbiertas", torneo.inscripcionesAbiertas());
        return json;
    }

    private String responder(Response response, int status, Object contenido) {
        response.status(status);
        response.type("application/json");
        return gson.toJson(contenido);
    }

    private void error(Response response, int status, String mensaje) {
        response.status(status);
        response.type("application/json");
        response.body(gson.toJson(Map.of("error", mensaje == null ? "Error" : mensaje)));
    }

    private static class ProductoRequest {
        String nombre;
        BigDecimal precioBaseUSD;
        String rareza;
        String tipo;
        String descripcion;
        String tipoAccesorio;
        String marca;
    }

    private static class ClienteRequest {
        String username;
        String password;
    }

    private static class CarritoRequest {
        Long productoId;
    }

    private static class TorneoRequest {
        String nombre;
        String fechaLimiteInscripcion;
        Integer cupoMaximo;
    }

    private static class UnauthorizedException extends RuntimeException {
        UnauthorizedException(String message) {
            super(message);
        }
    }
}
