package com.universidad.enviosservice.estrategia;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnvioExpressStrategyTest {

    private EnvioExpressStrategy estrategia;

    @BeforeEach
    void setUp() {
        estrategia = new EnvioExpressStrategy();
    }

    @ParameterizedTest(name = "Peso: {0} kg, Distancia: {1} km -> Costo esperado: {2}")
    @CsvSource({
            "3.0, 100.0, 156.0",     // Tramo 1: (100*1.2) * 1.3 = 120 * 1.3 = 156.0
            "5.0, 100.0, 156.0",     // Frontera Tramo 1: peso = 5.0 kg
            "10.0, 100.0, 165.75",   // Tramo 2: (120 + 5*1.5) * 1.3 = 127.5 * 1.3 = 165.75
            "20.0, 100.0, 185.25",   // Frontera Tramo 2: (120 + 15*1.5) * 1.3 = 142.5 * 1.3 = 185.25
            "25.0, 100.0, 198.25",   // Tramo 3: (120 + 22.5 + 5*2.0) * 1.3 = 152.5 * 1.3 = 198.25
            "4.0, 600.0, 968.5",     // Recargo distancia > 500 km: (600*1.2 + 25) * 1.3 = 745 * 1.3 = 968.5
            "10.0, 600.0, 978.25",   // Recargo distancia > 500 km con tramo 2: (720 + 7.5 + 25) * 1.3 = 752.5 * 1.3 = 978.25
            "25.0, 600.0, 1010.75"   // Recargo distancia > 500 km con tramo 3: (720 + 22.5 + 10 + 25) * 1.3 = 777.5 * 1.3 = 1010.75
    })
    @DisplayName("Calcula costo express con tramos de peso, recargo por distancia y factor 1.3")
    void calcularCostoExpress(double pesoKg, double distanciaKm, double costoEsperado) {
        double costo = estrategia.calcular(pesoKg, distanciaKm);
        assertEquals(costoEsperado, costo, 0.001);
    }
}
