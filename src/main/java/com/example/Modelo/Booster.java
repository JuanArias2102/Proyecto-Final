package com.example.Modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class Booster extends Producto {
 private static final int CARTAS_POR_BOOSTER = 10;
    private static final Random RANDOM = new Random();

    private List<Carta> contenido = new ArrayList<>();
    private Long compradorId;
    private boolean vendido;
    private boolean abierto;

    public Booster(Long id, String nombre, BigDecimal precioBaseUSD) {
        super(id, nombre, precioBaseUSD);
    }

    
    public List<Carta> generarCartas(List<Carta> cartasRegistradas) {
        if (cartasRegistradas == null || cartasRegistradas.isEmpty()) {
            throw new IllegalStateException("No hay cartas registradas para crear el booster");
        }
        Set<Rareza> disponibles = EnumSet.noneOf(Rareza.class);
        for (Carta c : cartasRegistradas) disponibles.add(c.getRareza());

        List<Carta> resultado = new ArrayList<>();
        for (int i = 0; i < CARTAS_POR_BOOSTER; i++) {
            Rareza sorteada = Rareza.sortear(RANDOM);
            Rareza usada = Rareza.resolverConRespaldo(sorteada, disponibles);
            List<Carta> candidatas = cartasRegistradas.stream().filter(c -> c.getRareza() == usada).collect(Collectors.toList());
            resultado.add(candidatas.get(RANDOM.nextInt(candidatas.size())));
        }
        this.contenido = resultado;
        return resultado;
    }

    public void marcarVendido(Long clienteId) {
        if (vendido) throw new IllegalStateException("El booster ya fue vendido");
        vendido = true;
        compradorId = clienteId;
    }

    
    public List<Carta> abrirBooster(Long clienteId) {
        if (!vendido || !clienteId.equals(compradorId)) {
            throw new SecurityException("Este booster no te pertenece");
        }
        if (abierto) throw new IllegalStateException("El booster ya fue abierto");
        abierto = true;
        return contenido;
    }

    
    public List<Carta> verContenido() {
        return abierto ? contenido : null;
    }

    public Long getCompradorId() { 
        return compradorId;
     }
    public boolean isVendido() { 
        return vendido; 
    }
    public boolean isAbierto() { 
        return abierto; 
    }
}
