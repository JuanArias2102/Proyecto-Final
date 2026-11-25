package com.example;

import java.io.IOException;
import java.nio.file.Path;

import com.example.Integraciones.TasaCambioService;
import com.example.Modelo.Administrador;
import com.example.Modelo.Rol;
import com.example.Modelo.Tienda;
import com.example.Modelo.Usuario;
import com.example.Persistencia.RepositorioJSON;
import com.google.gson.Gson;

import static spark.Spark.port;

public class Main {
    public static void main(String[] args) throws IOException {
        String rutaDatos = System.getenv("DATA_FILE");
        Path archivoDatos = rutaDatos == null || rutaDatos.isBlank()
                ? Path.of("datos.json")
                : Path.of(rutaDatos);
        RepositorioJSON repositorio = new RepositorioJSON(archivoDatos);
        Tienda tienda = repositorio.cargar();
        TasaCambioService tasaCambioService = new TasaCambioService();
        Gson gson = new Gson();

        boolean administradorExiste = false;
        for (Usuario usuario : tienda.getUsuarios()) {
            if (usuario.getRol() == Rol.ADMIN) {
                administradorExiste = true;
                break;
            }
        }

        if (!administradorExiste) {
            Administrador admin = new Administrador(
                    tienda.nuevoId(), "admin", "admin123");
            tienda.getUsuarios().add(admin);
            repositorio.guardar(tienda);
            System.out.println("Administrador inicial creado: usuario=admin, clave=admin123");
        }

        int cantidadClientes = 0;
        for (Usuario usuario : tienda.getUsuarios()) {
            if (usuario.getRol() == Rol.CLIENTE) {
                cantidadClientes++;
            }
        }
        System.out.println("Datos cargados. Productos: " + tienda.getProductos().size()
                + ", clientes: " + cantidadClientes
                + ", torneos: " + tienda.getTorneos().size());

        int puerto = leerPuerto();
        port(puerto);

        new ApiRest(tienda, repositorio, tasaCambioService, gson).registrarRutas();

        System.out.println("API iniciada en http://localhost:" + puerto);
    }

    private static int leerPuerto() {
        String puerto = System.getenv("PORT");
        if (puerto == null || puerto.isBlank()) {
            return 4567;
        }
        try {
            return Integer.parseInt(puerto);
        } catch (NumberFormatException excepcion) {
            return 4567;
        }
    }
}