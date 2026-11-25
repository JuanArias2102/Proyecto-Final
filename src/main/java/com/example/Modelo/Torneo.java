package com.example.Modelo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Torneo {
    private Long id;
    private String nombre;
    private Date fechaLimiteInscripcion;
    private Integer cupoMaximo;
    private Long administradorId;
    private List<Usuario> participantes = new ArrayList<>();

    public Torneo(Long id, String nombre, Date fechaLimiteInscripcion, Integer cupoMaximo,
                  Long administradorId) {
        if (nombre == null || nombre.isBlank() || fechaLimiteInscripcion == null
                || cupoMaximo == null || cupoMaximo <= 0 || administradorId == null) {
            throw new IllegalArgumentException("Datos del torneo inválidos");
        }
        this.id = id;
        this.nombre = nombre;
        this.fechaLimiteInscripcion = fechaLimiteInscripcion;
        this.cupoMaximo = cupoMaximo;
        this.administradorId = administradorId;
    }

    public boolean inscribirUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }
        if (usuario.getRol() != Rol.CLIENTE) {
            throw new SecurityException("Solo los clientes pueden inscribirse");
        }
        if (!inscripcionesAbiertas()) {
            throw new IllegalStateException("El plazo de inscripción ya venció");
        }
        if (!validarCupo()) {
            throw new IllegalStateException("El torneo no tiene cupos disponibles");
        }
        for (Usuario p : participantes) {
            if (p.getId().equals(usuario.getId())) {
                throw new IllegalStateException("El cliente ya está inscrito");
            }
        }
        participantes.add(usuario);
        return true;
    }

    public boolean inscribirCliente(Cliente cliente) {
        return inscribirUsuario(cliente);
    }

    public boolean validarCupo() {
        return participantes.size() < cupoMaximo;
    }

    public boolean inscripcionesAbiertas() {
        return new Date().compareTo(fechaLimiteInscripcion) <= 0;
    }

    public List<Usuario> obtenerParticipantes() {
        return participantes;
    }

    public void eliminarParticipante(Long usuarioId) {
        participantes.removeIf(participante -> participante.getId().equals(usuarioId));
    }

    public void restaurarParticipantes(List<Usuario> usuarios) {
        participantes.clear();
        participantes.addAll(usuarios);
    }

    public Long getId() { 
        return id;
     }
    public String getNombre() {
         return nombre; 
        }
    public Date getFechaLimiteInscripcion() { 
        return fechaLimiteInscripcion;
     }
    public Integer getCupoMaximo() { 
        return cupoMaximo; 
    }
    public Long getAdministradorId() {
        return administradorId;
    }
}
