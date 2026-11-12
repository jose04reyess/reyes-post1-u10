package com.universidad.enviosservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
