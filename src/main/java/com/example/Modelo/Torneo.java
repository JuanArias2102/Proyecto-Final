package com.example.Modelo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Torneo {
    private Long id;
    private String nombre;
    private Date fecha;
    private Date fechaLimiteInscripcion;
    private Integer cupoMaximo;
    private List<Usuario> participantes = new ArrayList<>();

    public Torneo(Long id, String nombre, Date fecha, Date fechaLimiteInscripcion, Integer cupoMaximo) {
        if (nombre == null || nombre.isBlank() || fecha == null || fechaLimiteInscripcion == null|| cupoMaximo == null || cupoMaximo <= 0
            || fechaLimiteInscripcion.after(fecha)) {
            throw new IllegalArgumentException("Datos del torneo inválidos");
        }
        this.id = id;
        this.nombre = nombre;
        this.fecha = fecha;
        this.fechaLimiteInscripcion = fechaLimiteInscripcion;
        this.cupoMaximo = cupoMaximo;
    }

    public boolean inscribirUsuario(Usuario usuario) {
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

    public boolean validarCupo() {
        return participantes.size() < cupoMaximo;
    }

    public boolean inscripcionesAbiertas() {
        return new Date().compareTo(fechaLimiteInscripcion) <= 0;
    }

    public List<Usuario> obtenerParticipantes() {
        return participantes;
    }

    public Long getId() { 
        return id;
     }
    public String getNombre() {
         return nombre; 
        }
    public Date getFecha() { 
        return fecha;
     }
    public Date getFechaLimiteInscripcion() { 
        return fechaLimiteInscripcion;
     }
    public Integer getCupoMaximo() { 
        return cupoMaximo; 
    }
}
