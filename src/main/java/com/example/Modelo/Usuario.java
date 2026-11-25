package com.example.Modelo;

import java.util.Objects;

public abstract class Usuario {
    private Long id;
    private String username;
    private String password;
    private Rol rol;

    public Usuario(Long id, String username, String password, Rol rol) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.rol = rol;
    }

    public boolean iniciarSesion(String username, String password) {
        return Objects.equals(this.username, username) && Objects.equals(this.password, password);
    }

    public void cerrarSesion() {
        
    }

    public Long getId() { 
        return id; 
    }
    public String getUsername() { 
        return username; 
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() { 
        return password; 
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public Rol getRol() { 
        return rol; 
    }
}
