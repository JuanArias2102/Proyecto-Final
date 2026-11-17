package com.example.Modelo;

import java.util.Random;
import java.util.Set;

public enum Rareza {
    NORMAL("N", 50),
    POCO_COMUN("UC", 25),
    RARO("R", 15),
    SUPER_RARO("SR", 7),
    ULTRA_RARO("UR", 3);

    private final String codigo;
    private final int probabilidad;

    Rareza(String codigo, int probabilidad) {
        this.codigo = codigo;
        this.probabilidad = probabilidad;
    }

    static {
        int suma = 0;
        for (Rareza r : values()) suma += r.probabilidad;
        if (suma != 100) throw new IllegalStateException("Las probabilidades deben sumar 100");
    }

    public String getCodigo() { return codigo; }
    public int getProbabilidad() { return probabilidad; }

    /** Sortea una rareza según la tabla de probabilidades. */
    public static Rareza sortear(Random random) {
        int n = random.nextInt(100); // 0..99
        int acumulado = 0;
        for (Rareza r : values()) {
            acumulado += r.probabilidad;
            if (n < acumulado) return r;
        }
        return NORMAL;
    }
    public static Rareza resolverConRespaldo(Rareza sorteada, Set<Rareza> disponibles) {
        if (disponibles.contains(sorteada)) return sorteada;
        Rareza[] todas = values();
        for (int i = sorteada.ordinal() - 1; i >= 0; i--) {
            if (disponibles.contains(todas[i])) return todas[i];
        }
        for (int i = sorteada.ordinal() + 1; i < todas.length; i++) {
            if (disponibles.contains(todas[i])) return todas[i];
        }
        throw new IllegalStateException("No hay cartas registradas");
    }
}
