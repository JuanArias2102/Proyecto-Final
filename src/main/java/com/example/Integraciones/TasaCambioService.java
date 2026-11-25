package com.example.Integraciones;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TasaCambioService {
    private final HttpClient cliente;
    private final String apiKey;
    private final BigDecimal tasaDefecto;

    public TasaCambioService() {
        this.apiKey = System.getenv("EXCHANGE_API_KEY");
        this.tasaDefecto = leerTasaDefecto();
        this.cliente = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    public ResultadoTasa obtenerTasaUsdCop() {
        if (apiKey == null || apiKey.isBlank()) {
            return usarTasaDefecto("La clave EXCHANGE_API_KEY no esta configurada");
        }

        try {
            String url = "https://v6.exchangerate-api.com/v6/"
                    + apiKey.trim() + "/latest/USD";
            HttpRequest solicitud = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> respuesta = cliente.send(
                    solicitud, HttpResponse.BodyHandlers.ofString());

            if (respuesta.statusCode() != 200) {
                return usarTasaDefecto("La API respondio con HTTP "
                        + respuesta.statusCode());
            }

            JsonObject json = JsonParser.parseString(respuesta.body()).getAsJsonObject();
            if (!"success".equals(json.get("result").getAsString())) {
                return usarTasaDefecto("La API no devolvio un resultado exitoso");
            }

            JsonObject conversiones = json.getAsJsonObject("conversion_rates");
            BigDecimal tasa = conversiones.get("COP").getAsBigDecimal();
            if (tasa.compareTo(BigDecimal.ZERO) <= 0) {
                return usarTasaDefecto("La API devolvio una tasa invalida");
            }
            return new ResultadoTasa(tasa, "API");
        } catch (Exception excepcion) {
            return usarTasaDefecto("No fue posible consultar la API");
        }
    }

    private ResultadoTasa usarTasaDefecto(String motivo) {
        System.out.println("La API no respondio, por lo que se toma un valor por defecto de "
                + tasaDefecto + ". " + motivo);
        return new ResultadoTasa(tasaDefecto, "DEFECTO");
    }

    private BigDecimal leerTasaDefecto() {
        String valor = System.getenv("TASA_DEFECTO");
        if (valor == null || valor.isBlank()) {
            return new BigDecimal("4000");
        }
        try {
            BigDecimal tasa = new BigDecimal(valor.trim());
            if (tasa.compareTo(BigDecimal.ZERO) > 0) {
                return tasa;
            }
        } catch (NumberFormatException ignored) {
        }
        return new BigDecimal("4000");
    }

    public static class ResultadoTasa {
        private final BigDecimal tasa;
        private final String fuenteTasa;

        public ResultadoTasa(BigDecimal tasa, String fuenteTasa) {
            this.tasa = tasa;
            this.fuenteTasa = fuenteTasa;
        }

        public BigDecimal getTasa() {
            return tasa;
        }

        public String getFuenteTasa() {
            return fuenteTasa;
        }
    }
}
