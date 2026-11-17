package com.example.Controladores;
import java.util.List;

import com.example.Modelo.Administrador;
import com.example.Modelo.Cliente;


public class UsuarioControladores {
    
  public Cliente crearCliente(Administrador admin, String username, String password) {
        if (vacio(username) || vacio(password)) {
            throw new IllegalArgumentException("username y password son obligatorios");
        }
        Cliente cliente = admin.registrarCliente(Sistema.datos.nuevoId(), username, password, Sistema.datos.getUsuarios());
        Sistema.guardar();
        return cliente;
    }

    
    public List<Cliente> listarClientes() {
        return Sistema.datos.getClientes();
    }

    
    public Cliente buscarCliente(Administrador admin, Long id) {
        Cliente cliente = admin.buscarCliente(id, Sistema.datos.getUsuarios());
        if (cliente == null) {
            throw new NoEncontradoException("El cliente no existe");
        }
        return cliente;
    }

    
    public Cliente actualizarCliente(Administrador admin, Long id, String username, String password) {
        if (username == null && password == null) {
            throw new IllegalArgumentException("Envíe username y/o password para actualizar");
        }
        if ((username != null && vacio(username)) || (password != null && vacio(password))) {
            throw new IllegalArgumentException("Los campos no pueden estar vacíos");
        }
        Cliente cliente = buscarCliente(admin, id);
        admin.actualizarCliente(cliente, username, password, Sistema.datos.getUsuarios());
        Sistema.guardar();
        return cliente;
    }

    
    public void eliminarCliente(Administrador admin, Long id) {
        Cliente cliente = buscarCliente(admin, id);
        admin.eliminarCliente(cliente, Sistema.datos.getUsuarios(), Sistema.datos.getTorneos());
        Sistema.guardar();
    }

    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
