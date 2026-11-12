package com.universidad.enviosservice.estrategia;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnvioInternacionalStrategyTest {

    private EnvioInternacionalStrategy estrategia;

    @BeforeEach
    void setUp() {
        estrategia = new EnvioInternacionalStrategy();
    }

    @ParameterizedTest(name = "Peso: {0} kg, Distancia: {1} km -> Costo esperado: {2}")
    @CsvSource({
            "3.0, 1000.0, 1850.0",     // Tramo 1: 1000*1.8 + 50 = 1850.0
            "5.0, 1000.0, 1850.0",     // Frontera Tramo 1: peso = 5.0 kg
            "10.0, 1000.0, 1865.0",    // Tramo 2: 1800 + 50 + 5*3.0 = 1865.0
            "20.0, 1000.0, 1895.0",    // Frontera Tramo 2: 1800 + 50 + 15*3.0 = 1895.0
            "25.0, 1000.0, 1917.5",    // Tramo 3: 1800 + 50 + 45 + 5*4.5 = 1917.5
            "4.0, 5000.0, 9050.0",     // Frontera distancia: exactamente 5000 km (sin recargo)
            "4.0, 6000.0, 10950.0",    // Recargo distancia > 5000 km: 6000*1.8 + 50 + 100 = 10950.0
            "10.0, 6000.0, 10965.0",   // Recargo distancia > 5000 km con tramo 2
            "25.0, 6000.0, 11017.5"    // Recargo distancia > 5000 km con tramo 3
    })
    @DisplayName("Calcula costo internacional con tramos de peso y recargo por distancia > 5000 km")
    void calcularCostoInternacional(double pesoKg, double distanciaKm, double costoEsperado) {
        double costo = estrategia.calcular(pesoKg, distanciaKm);
        assertEquals(costoEsperado, costo, 0.001);
    }
}
