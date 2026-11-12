package com.universidad.enviosservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServicioCalculoEnvioTest {

    private ServicioCalculoEnvio servicio;

    @BeforeEach
    void setUp() {
        servicio = new ServicioCalculoEnvio();
    }

    @Test
    @DisplayName("Calcula envio estandar para peso bajo")
    void calculaEnvioEstandarPesoBajo() {
        double costo = servicio.calcularCosto("ESTANDAR", 3.0, 100.0, "REGULAR", false);
        assertEquals(50.0, costo, 0.001);
    }

    @Test
    @DisplayName("Aplica descuento a cliente premium")
    void aplicaDescuentoClientePremium() {
        double costo = servicio.calcularCosto("ESTANDAR", 3.0, 100.0, "PREMIUM", false);
        assertEquals(45.0, costo, 0.001);
    }

    @Test
    @DisplayName("Aplica descuento a cliente corporativo")
    void aplicaDescuentoClienteCorporativo() {
        double costo = servicio.calcularCosto("ESTANDAR", 3.0, 100.0, "CORPORATIVO", false);
        assertEquals(42.5, costo, 0.001);
    }

    @Test
    @DisplayName("Aplica recargo por fragilidad sin descuento")
    void aplicaRecargoFragilidad() {
        double costo = servicio.calcularCosto("ESTANDAR", 3.0, 100.0, "REGULAR", true);
        assertEquals(57.5, costo, 0.001);
    }

    @Test
    @DisplayName("Aplica recargo por fragilidad y descuento a cliente premium")
    void aplicaFragilidadYDescuentoPremium() {
        // Base: 50.0; con fragilidad (+15%): 57.5; con descuento premium (-10%): 57.5 * 0.9 = 51.75
        double costo = servicio.calcularCosto("ESTANDAR", 3.0, 100.0, "PREMIUM", true);
        assertEquals(51.75, costo, 0.001);
    }

    @Test
    @DisplayName("Aplica recargo por fragilidad y descuento a cliente corporativo")
    void aplicaFragilidadYDescuentoCorporativo() {
        // Base: 50.0; con fragilidad (+15%): 57.5; con descuento corporativo (-15%): 57.5 * 0.85 = 48.875
        double costo = servicio.calcularCosto("ESTANDAR", 3.0, 100.0, "CORPORATIVO", true);
        assertEquals(48.875, costo, 0.001);
    }

    @Test
    @DisplayName("Lanza IllegalArgumentException cuando el metodo de envio no esta soportado")
    void lanzaExcepcionMetodoNoSoportado() {
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> servicio.calcularCosto("DRON", 2.0, 50.0, "REGULAR", false)
        );
        assertEquals("Metodo de envio no soportado: DRON", excepcion.getMessage());
    }

    @ParameterizedTest(name = "Metodo: {0} delega correctamente a su estrategia correspondiente")
    @CsvSource({
            "ESTANDAR, 4.0, 100.0, 50.0",
            "EXPRESS, 4.0, 100.0, 156.0",
            "OVERNIGHT, 4.0, 100.0, 220.0",
            "INTERNACIONAL, 4.0, 1000.0, 1850.0"
    })
    @DisplayName("Verifica delegacion correcta a cada estrategia desde el orquestador")
    void delegaCorrectamenteACadaEstrategia(String metodo, double pesoKg, double distanciaKm, double costoEsperado) {
        double costo = servicio.calcularCosto(metodo, pesoKg, distanciaKm, "REGULAR", false);
        assertEquals(costoEsperado, costo, 0.001);
    }
}
