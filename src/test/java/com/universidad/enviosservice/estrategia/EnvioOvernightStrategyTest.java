package com.universidad.enviosservice.estrategia;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnvioOvernightStrategyTest {

    private EnvioOvernightStrategy estrategia;

    @BeforeEach
    void setUp() {
        estrategia = new EnvioOvernightStrategy();
    }

    @ParameterizedTest(name = "Peso: {0} kg, Distancia: {1} km -> Costo esperado: {2}")
    @CsvSource({
            "3.0, 100.0, 220.0",   // Tramo 1: 100*2.0 + 20 = 220.0
            "5.0, 100.0, 220.0",   // Frontera Tramo 1: peso = 5.0 kg
            "10.0, 100.0, 232.5",  // Tramo 2: 200 + 20 + 5*2.5 = 232.5
            "20.0, 100.0, 257.5",  // Frontera Tramo 2: 200 + 20 + 15*2.5 = 257.5
            "25.0, 100.0, 272.5",  // Tramo 3: 200 + 20 + 37.5 + 5*3.0 = 272.5
            "5.0, 300.0, 620.0"    // Frontera distancia permitida: distancia = 300 km
    })
    @DisplayName("Calcula costo overnight para tramos de peso dentro del límite de 300 km")
    void calcularCostoOvernight(double pesoKg, double distanciaKm, double costoEsperado) {
        double costo = estrategia.calcular(pesoKg, distanciaKm);
        assertEquals(costoEsperado, costo, 0.001);
    }

    @Test
    @DisplayName("Lanza IllegalArgumentException cuando distancia supera 300 km")
    void lanzaExcepcionDistanciaExcedida() {
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> estrategia.calcular(5.0, 301.0)
        );
        assertEquals("OVERNIGHT no disponible para mas de 300 km", excepcion.getMessage());
    }
}

