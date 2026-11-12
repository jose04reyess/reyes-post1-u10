package com.universidad.enviosservice.estrategia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EnvioEstandarStrategyTest {

    private EnvioEstandarStrategy estrategia;

    @BeforeEach
    void setUp() {
        estrategia = new EnvioEstandarStrategy();
    }

    @ParameterizedTest(name = "Peso: {0} kg, Distancia: {1} km -> Costo esperado: {2}")
    @CsvSource({
            "3.0, 100.0, 50.0",     // Tramo 1: peso <= 5 kg, distancia <= 500 km
            "5.0, 100.0, 50.0",     // Frontera Tramo 1: exactamente 5 kg
            "10.0, 100.0, 54.0",    // Tramo 2: 5 < peso <= 20 kg (50 + 5*0.8)
            "20.0, 100.0, 62.0",    // Frontera Tramo 2: exactamente 20 kg (50 + 15*0.8)
            "25.0, 100.0, 68.0",    // Tramo 3: peso > 20 kg (50 + 12 + 5*1.2)
            "4.0, 600.0, 310.0",    // Recargo distancia > 500 km (600*0.5 + 10)
            "10.0, 600.0, 314.0",   // Recargo distancia > 500 km con tramo 2
            "25.0, 600.0, 328.0"    // Recargo distancia > 500 km con tramo 3
    })
    @DisplayName("Calcula costo estándar para diferentes tramos de peso y distancias")
    void calcularCostoEstandar(double pesoKg, double distanciaKm, double costoEsperado) {
        double costo = estrategia.calcular(pesoKg, distanciaKm);
        assertEquals(costoEsperado, costo, 0.001);
    }
}

