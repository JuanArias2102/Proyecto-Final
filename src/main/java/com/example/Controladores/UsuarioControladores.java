package com.example.Controladores;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.example.Modelo.Administrador;
import com.example.Modelo.Cliente;
import com.example.Modelo.Tienda;
import com.example.Modelo.Usuario;
import com.example.Persistencia.RepositorioJSON;

public class UsuarioControladores {
    private final Tienda tienda;
    private final RepositorioJSON repositorio;

    public UsuarioControladores(Tienda tienda, RepositorioJSON repositorio) {
        this.tienda = tienda;
        this.repositorio = repositorio;
    }

    public Cliente crearCliente(Administrador admin, String username, String password) throws IOException {
        validarAdministrador(admin);
        if (vacio(username) || vacio(password)) {
            throw new IllegalArgumentException("username y password son obligatorios");
        }
        Cliente cliente = admin.registrarCliente(tienda.nuevoId(), username, password, tienda.getUsuarios());
        guardar();
        return cliente;
    }

    public List<Cliente> listarClientes() {
        List<Cliente> clientes = new ArrayList<>();
        for (Usuario usuario : tienda.getUsuarios()) {
            if (usuario instanceof Cliente) {
                clientes.add((Cliente) usuario);
            }
        }
        return clientes;
    }

    public Cliente buscarCliente(Administrador admin, Long id) {
        validarAdministrador(admin);
        Cliente cliente = admin.buscarCliente(id, tienda.getUsuarios());
        if (cliente == null) {
            throw new NoEncontradoException("El cliente no existe");
        }
        return cliente;
    }

    public Cliente actualizarCliente(Administrador admin, Long id, String username, String password)
            throws IOException {
        validarAdministrador(admin);
        if (username == null && password == null) {
            throw new IllegalArgumentException("Envíe username y/o password para actualizar");
        }
        if ((username != null && vacio(username)) || (password != null && vacio(password))) {
            throw new IllegalArgumentException("Los campos no pueden estar vacíos");
        }
        Cliente cliente = buscarCliente(admin, id);
        admin.actualizarCliente(cliente, username, password, tienda.getUsuarios());
        guardar();
        return cliente;
    }

    public void eliminarCliente(Administrador admin, Long id) throws IOException {
        validarAdministrador(admin);
        Cliente cliente = buscarCliente(admin, id);
        admin.eliminarCliente(cliente, tienda.getUsuarios(), tienda.getTorneos());
        guardar();
    }

    private void validarAdministrador(Administrador admin) {
        if (admin == null || admin.getRol() != com.example.Modelo.Rol.ADMIN
                || !tienda.getUsuarios().contains(admin)) {
            throw new SecurityException("Se requiere un administrador registrado");
        }
    }

    private void guardar() throws IOException {
        repositorio.guardar(tienda);
    }

    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
