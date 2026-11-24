package com.example.Controladores;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.example.Modelo.Administrador;
import com.example.Modelo.Cliente;
import com.example.Modelo.Tienda;
import com.example.Modelo.Torneo;
import com.example.Persistencia.RepositorioJSON;

public class TorneoControladores {
    private final Tienda tienda;
    private final RepositorioJSON repositorio;

    public TorneoControladores(Tienda tienda, RepositorioJSON repositorio) {
        this.tienda = tienda;
        this.repositorio = repositorio;
    }

    public Torneo crearTorneo(Administrador admin, String nombre, Date fechaLimiteInscripcion,
                              Integer cupoMaximo) throws IOException {
        if (admin == null || admin.getRol() != com.example.Modelo.Rol.ADMIN
                || !tienda.getUsuarios().contains(admin)) {
            throw new SecurityException("Se requiere un administrador registrado");
        }
        Torneo torneo = admin.crearTorneo(tienda.nuevoId(),
                nombre, fechaLimiteInscripcion, cupoMaximo);
        tienda.getTorneos().add(torneo);
        guardar();
        return torneo;
    }

    public List<Torneo> listarTorneos() {
        return new java.util.ArrayList<>(tienda.getTorneos());
    }

    public Torneo inscribirCliente(Cliente cliente, Long torneoId) throws IOException {
        if (cliente == null || !tienda.getUsuarios().contains(cliente)) {
            throw new SecurityException("El cliente debe estar registrado");
        }
        Torneo torneo = buscarTorneo(torneoId);
        cliente.inscribirseEnTorneo(torneo);
        guardar();
        return torneo;
    }

    public Torneo buscarTorneo(Long id) {
        for (Torneo torneo : tienda.getTorneos()) {
            if (torneo.getId().equals(id)) {
                return torneo;
            }
        }
        throw new NoEncontradoException("El torneo no existe");
    }

    private void guardar() throws IOException {
        repositorio.guardar(tienda);
    }
}
