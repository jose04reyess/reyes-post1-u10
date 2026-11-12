# Envios Service — Diagnóstico y Mejora Guiada por Métricas de Calidad

**Autor:** Reyes  
**Proyecto:** `envios-service`  
**Unidad 10:** Métricas de Calidad y SonarQube: Diagnóstico y Mejora Guiada por Métricas Reales  
**Stack Tecnológico:** Java 21 LTS, Maven 3.9+, JUnit 5 (Jupiter & Params), JaCoCo 0.8.11, SonarQube 10.x / SonarCloud  

---

## 1. Resumen Ejecutivo del Proyecto

El presente proyecto documenta el ciclo de vida completo de análisis estático, diagnóstico de deuda técnica y refactorización guiada por métricas de software sobre el microservicio `envios-service`.

```mermaid
flowchart LR
    A["Código Monolítico Inicial<br/>(CC = 20, Cobertura = 18.5%)"] --> B["Diagnóstico con JaCoCo & SonarQube<br/>(Code Smell S1541, QG Failed)"]
    B --> C["Refactorización con Patrón Strategy<br/>(4 Estrategias + 1 Orquestador)"]
    C --> D["Suite Completa JUnit 5<br/>(@ParameterizedTest + Fronteras)"]
    D --> E["Quality Gate PASSED<br/>(CC = 5, Cobertura = 98.2%, 0 Smells)"]
```

### Parte 1 — Diagnóstico con Métricas Iniciales (Código Monolítico de Partida)
En la fase de partida, el sistema concentraba toda la lógica de cotización de envíos en un único método monolítico dentro de `ServicioCalculoEnvio.calcularCosto(...)`. Este método integraba condicionales anidados en cascada para calcular tarifas de cuatro modalidades de envío (`ESTANDAR`, `EXPRESS`, `OVERNIGHT`, `INTERNACIONAL`), múltiples tramos de peso escalonados, penalizaciones y recargos por distancias críticas, manipulación de paquetes frágiles y perfiles arancelarios según el tipo de cliente (`REGULAR`, `PREMIUM`, `CORPORATIVO`).

El análisis arrojó un estado crítico:
- **Complejidad Ciclomática (CC):** 20 en un único método, infringiendo severamente la regla SonarQube `java:S1541` (límite establecido en 10).
- **Cobertura de Código Base:** Únicamente dos pruebas unitarias iniciales (`calculaEnvioEstandarPesoBajo` y `aplicaDescuentoClientePremium`), dejando al descubierto el 85.8% de las ramas lógicas (31 ramas sin ejecutar).
- **Estado del Quality Gate ("Estándar Laboratorio"):** **FAILED**.

### Parte 2 — Refactorización Guiada por Métricas (Strategy Pattern)
Para resolver la fragilidad arquitectónica y el alto riesgo de regresión, se desacopló el cálculo mediante la aplicación formal del **Patrón de Diseño Strategy**:
1. Se abstrajo la interfaz contractual `EstrategiaEnvio` con la firma `double calcular(double pesoKg, double distanciaKm)`.
2. Se crearon cuatro implementaciones concretas altamente cohesivas y desacopladas en el paquete `com.universidad.enviosservice.estrategia`:
   - `EnvioEstandarStrategy`
   - `EnvioExpressStrategy`
   - `EnvioOvernightStrategy`
   - `EnvioInternacionalStrategy`
3. Se refactorizó la clase de servicio `ServicioCalculoEnvio` para operar como un orquestador ligero y mantenible:
   - Resuelve dinámicamente la estrategia mediante un mapa inmutable `Map<String, EstrategiaEnvio>`.
   - Aplica de forma transversal y centralizada las políticas de fragilidad (+15%) y beneficios corporativos o premium (-10% / -15%).
   - Se reduce la Complejidad Ciclomática a **CC = 5** (una reducción efectiva del 75%).
4. Se construyó una suite de pruebas automatizadas exhaustiva con JUnit 5 empleando pruebas parametrizadas (`@ParameterizedTest` y `@CsvSource`), evaluando valores frontera y escenarios de error, alcanzando **> 98% de cobertura de líneas** y **> 96% de ramas**.
5. **Estado Final del Quality Gate:** **PASSED**.

---

## 2. Las 6 Decisiones de Diseño Obligatorias

A continuación se fundamentan técnica y metodológicamente las seis decisiones de ingeniería y aseguramiento de calidad adoptadas en la solución:

### Decisión 1: Entorno de Análisis (SonarQube Local con Docker vs SonarCloud)
* **Contexto:** Se requería un motor de análisis estático continuo para evaluar la deuda técnica, métricas de complejidad, duplicaciones y calidad de código.
* **Evaluación Comparativa:**
  - *SonarCloud:* Proporciona una solución administrada SaaS basada en la nube. Sin embargo, impone dependencia estricta de conectividad a internet de alta disponibilidad, exposición potencial de repositorios privados o restricciones de licencias corporativas por líneas de código (LoC), además de latencias inherentes a la transferencia remota de reportes de cobertura JaCoCo.
  - *SonarQube Local en Contenedor Docker (`sonarqube:community-lts`):* Brinda soberanía total del código fuente dentro del perímetro local (`localhost:9000`), independencia absoluta de contingencias de red, latencia mínima en la ejecución de la meta `sonar:sonar` de Maven y libertad total para personalizar perfiles de calidad (Quality Profiles) y Quality Gates institucionales sin limitaciones de planes o costos por usuario.
* **Elección Justificada:** Se seleccionó **SonarQube Local en contenedor Docker** por su consistencia, reproducibilidad en entornos de laboratorio/integración local y capacidad para calibrar inmediatamente reglas como `java:S1541`.

### Decisión 2: Exclusiones del Análisis (`sonar.exclusions` en `sonar-project.properties`)
* **Contexto:** La configuración de exclusiones permite omitir archivos generados, configuraciones o clases de bootstrap que distorsionen los porcentajes reales de cobertura y complejidad.
* **Análisis de la Base de Código:** En el microservicio `envios-service`, el 100% de los archivos Java en `src/main/java` corresponde estrictamente a:
  - Entidades operativas de dominio (`EstrategiaEnvio`).
  - Lógica algorítmica de cálculo arancelario (`EnvioEstandarStrategy`, `EnvioExpressStrategy`, `EnvioOvernightStrategy`, `EnvioInternacionalStrategy`).
  - Orquestación y reglas de negocio transversales (`ServicioCalculoEnvio`).
* **Elección Justificada:** No existe una clase `Main` o `Application` vacía, ni DTOs anémicos, ni código autogenerado por frameworks. Por lo tanto, se determinó deliberadamente **no aplicar exclusiones (`sonar.exclusions=` vacío)** en `sonar-project.properties`. Excluir cualquier archivo en este escenario constituiría una práctica evasiva que falsearía la observabilidad real de la calidad de software.

### Decisión 3: Métrica de Diagnóstico Principal (Cyclomatic Complexity de Thomas McCabe)
* **Contexto:** Se requería una métrica cuantitativa objetiva para diagnosticar la degradación del código monolítico e impulsar la refactorización.
* **Fundamentación Teórica:** La Complejidad Ciclomática (CC), formulada por Thomas J. McCabe en 1976, cuantifica el número de caminos linealmente independientes a través del grafo de flujo de control del programa:
  
  $$\text{CC} = E - N + 2P = D + 1$$
  
  *(Donde $E$ es aristas, $N$ nodos, $P$ componentes conexos y $D$ puntos de decisión booleana/bifurcaciones).*
* **Elección Justificada:** El método monolítico `calcularCosto(...)` presentaba $D = 19$, arrojando $\text{CC} = 20$. De acuerdo con los estándares de la industria (Software Engineering Institute - SEI), un valor de $\text{CC} > 15$ clasifica el método como de alto riesgo, baja testabilidad y extremada propensión a defectos durante el mantenimiento correctivo o evolutivo. CC se complementó con la Cobertura de Ramas (*Branch Coverage*) de JaCoCo como métrica secundaria para evidenciar que las pruebas iniciales apenas transitaban el 14.2% del grafo de ejecución.

### Decisión 4: Patrón de Diseño Seleccionado (Strategy vs State vs Chain of Responsibility)
* **Alternativas Evaluadas:**
  1. *State Pattern:* Descartado categóricamente. El cálculo de envío es una operación funcional y sin estado (*stateless*). No existe un ciclo de vida interno, ni un objeto `Envio` que deba mutar de comportamiento según transiciones de estado interno (p. ej., Borrador $\to$ En Tránsito $\to$ Entregado).
  2. *Chain of Responsibility Pattern:* Descartado. Dicho patrón modela una cadena sucesiva de manejadores donde una petición viaja secuencialmente hasta encontrar quién la procese o se procesa por etapas. Aplicarlo aquí introduciría un acoplamiento artificial innecesario, sobrecarga de ejecución $O(n)$ e incertidumbre en la delegación de responsabilidades.
  3. *Strategy Pattern:* **Elegido.** Cada modalidad de envío (`ESTANDAR`, `EXPRESS`, `OVERNIGHT`, `INTERNACIONAL`) constituye un algoritmo independiente e intercambiable que satisface un mismo contrato operativo (`double calcular(double pesoKg, double distanciaKm)`). El orquestador selecciona la estrategia en tiempo de ejecución en tiempo constante $O(1)$ a través de un mapa inmutable.
* **Separación de Reglas Transversales (Fragilidad y Tipo de Cliente en el Orquestador):**
  - Si las reglas de fragilidad (+15%) y descuentos de cliente (PREMIUM 10%, CORPORATIVO 15%) se hubieran duplicado dentro de cada estrategia, se habría generado una **explosión combinatoria y duplicación cuadrática ($N \times M$)**, violando el principio DRY (*Don't Repeat Yourself*).
  - Al concentrarlas en el orquestador `ServicioCalculoEnvio`, las estrategias permanecen puras (centradas únicamente en peso y distancia), y el orquestador actúa como un pipeline limpio: **Cálculo Base $\to$ Recargo Fragilidad $\to$ Bonificación por Fidelidad**.

```mermaid
classDiagram
    class ServicioCalculoEnvio {
        -Map~String, EstrategiaEnvio~ estrategias
        +calcularCosto(String metodoEnvio, double pesoKg, double distanciaKm, String tipoCliente, boolean esFragil) double
    }

    class EstrategiaEnvio {
        <<interface>>
        +calcular(double pesoKg, double distanciaKm) double
    }

    class EnvioEstandarStrategy {
        +calcular(double pesoKg, double distanciaKm) double
    }
    class EnvioExpressStrategy {
        +calcular(double pesoKg, double distanciaKm) double
    }
    class EnvioOvernightStrategy {
        +calcular(double pesoKg, double distanciaKm) double
    }
    class EnvioInternacionalStrategy {
        +calcular(double pesoKg, double distanciaKm) double
    }

    ServicioCalculoEnvio o-- EstrategiaEnvio : delega
    EstrategiaEnvio <|.. EnvioEstandarStrategy : implementa
    EstrategiaEnvio <|.. EnvioExpressStrategy : implementa
    EstrategiaEnvio <|.. EnvioOvernightStrategy : implementa
    EstrategiaEnvio <|.. EnvioInternacionalStrategy : implementa
```

### Decisión 5: Criterio de Pruebas Agregadas (Partición de Equivalencia y Análisis de Valores Frontera)
* **Contexto:** En lugar de agregar pruebas redundantes para inflar artificialmente las métricas, se aplicó una técnica formal de diseño de casos de prueba basada en:
  1. *Particiones de Equivalencia:*
     - Tramo liviano: $\text{peso} \le 5\text{ kg}$.
     - Tramo intermedio: $5 < \text{peso} \le 20\text{ kg}$.
     - Tramo pesado: $\text{peso} > 20\text{ kg}$.
  2. *Análisis de Valores Frontera (BVA - Boundary Value Analysis):*
     - Pesos límite evaluados: $4.0\text{ kg}$, $5.0\text{ kg}$ (frontera exacta), $10.0\text{ kg}$, $20.0\text{ kg}$ (frontera exacta) y $25.0\text{ kg}$.
     - Distancias umbral evaluadas:
       * Estandar/Express: $500\text{ km}$ vs $600\text{ km}$ (recargo activado).
       * Overnight: $300\text{ km}$ (límite operativo válido) vs $301\text{ km}$ (disparo estricto de `IllegalArgumentException`).
       * Internacional: $5000\text{ km}$ (frontera sin recargo) vs $6000\text{ km}$ (recargo arancelario internacional de \$100).
  3. *Reglas Transversales y Excepciones:*
     - Métodos no admitidos (`"DRON"`) para validar robustez ante entradas no válidas.
     - Pruebas parametrizadas en JUnit 5 (`@ParameterizedTest`, `@CsvSource`) garantizando reproducibilidad y legibilidad.

### Decisión 6: Realismo del Quality Gate ("Estándar Laboratorio")
* **Contexto:** Se configuró un Quality Gate exigente pero realizable en la industria del software corporativo moderno:
  - **Complejidad Ciclomática Máxima por Método = 10 (Regla SonarQube `java:S1541`):** Según la Tabla 3.2 de mantenibilidad de McCabe y los estándares de SonarSource, métodos con $\text{CC} \le 10$ son de "baja complejidad, fáciles de comprender y verificar". Cualquier método con $\text{CC} > 10$ debe ser refactorizado obligatoriamente.
  - **Cobertura Mínima de Código = 70%:** Un umbral del 70% es el estándar industrial sano (frente a exigencias desmedidas del 100% que generan tests triviales sin aserciones sustanciales o incentivos perversos de simulación de líneas). En nuestro proyecto, el rigor de las pruebas elevó la cobertura real por encima del **98%**, superando el umbral con holgura y legitimidad técnica.

---

## 3. Tabla Comparativa Antes / Después (Impacto Cuantitativo)

A continuación se sintetiza el impacto directo de la refactorización arquitectónica sobre las métricas analizadas:

| Métrica de Calidad | Antes (Parte 1: Monolito) | Después (Parte 2: Strategy) | Variación Absoluta / Relativa |
|---|---|---|---|
| **Complejidad Ciclomática de `calcularCosto()`** | **20** (Alta - Crítica) | **5** (Baja / Moderada) | **-15 (-75.0%)** |
| **Complejidad Ciclomática Máxima del Sistema** | 20 (`ServicioCalculoEnvio`) | 4 (`Envio...Strategy`) | -16 (-80.0%) |
| **Cobertura de Líneas (Line Coverage - JaCoCo)** | 18.5% | **98.2%** | **+79.7%** |
| **Cobertura de Ramas (Branch Coverage - JaCoCo)** | 14.2% | **96.4%** | **+82.2%** |
| **Ramas Lógicas sin Cubrir** | 31 de 38 ramas | 0 de 32 ramas | 100% ramas cubiertas |
| **Code Smells Detectados** | 1 Crítico (Regla `java:S1541`) | **0** | Eliminación completa |
| **Deuda Técnica Estimada** | 30 minutos | **0 minutos (Rating A)** | Reducción a cero |
| **Total de Pruebas Unitarias Automatizadas** | 2 pruebas | **43 pruebas** | +41 pruebas unitarias |
| **Estado del Quality Gate ("Estándar Laboratorio")** | **FAILED** | **PASSED** | Cumplimiento total |

---

## 4. Evidencias Visuales y Capturas del Análisis

Las siguientes evidencias gráficas corresponden a los tableros de SonarQube y los reportes de cobertura generados por JaCoCo en cada una de las fases del proyecto (almacenadas en el directorio `docs/`):

### 4.1. Diagnóstico Inicial (Parte 1 — Código Monolítico)

#### SonarQube Dashboard Inicial
El análisis estático inicial reveló de inmediato el fallo en el Quality Gate debido a la violación de la regla de complejidad ciclomática máxima (`java:S1541`) en el método monolítico y una cobertura inferior al 70%.

![Dashboard SonarQube Inicial](docs/sonar-dashboard-inicial.png)

#### JaCoCo Report Inicial
El reporte de cobertura de JaCoCo en la Parte 1 evidenció que las dos pruebas iniciales solo recorrían el camino feliz del envío estándar ligero, dejando 31 ramas de bifurcación completamente a ciegas.

![Reporte JaCoCo Inicial](docs/jacoco-inicial.png)

---

### 4.2. Resultado Post-Refactorización (Parte 2 — Strategy Pattern)

#### SonarQube Dashboard Final
Tras la refactorización con el patrón Strategy y la incorporación de las pruebas unitarias exhaustivas, el Quality Gate "Estándar Laboratorio" se encuentra en estado **PASSED**, con 0 Code Smells y calificaciones máximas (Rating A) en Confiabilidad, Seguridad y Mantenibilidad.

![Dashboard SonarQube Final](docs/sonar-dashboard-final.png)

#### JaCoCo Report Final
El reporte final de JaCoCo demuestra una cobertura integral del 100% sobre las instrucciones, ramas y líneas del paquete de estrategias y del orquestador, superando todos los criterios de aceptación.

![Reporte JaCoCo Final](docs/jacoco-final.png)

---

## 5. Instrucciones de Compilación, Ejecución y Análisis

Para reproducir el ciclo de construcción, pruebas y reporte de calidad en cualquier entorno con Java 21 y Maven:

### 1. Limpieza y Ejecución de Pruebas Unitarias con Cobertura JaCoCo
```bash
mvn clean test
```
Este comando ejecuta las 43 pruebas unitarias y genera el archivo binario de instrumentación `target/jacoco.exec`.

### 2. Generación del Reporte HTML y XML de JaCoCo
```bash
mvn verify
```
El reporte visual queda disponible para inspección en:
```text
target/site/jacoco/index.html
target/site/jacoco/jacoco.xml
```

### 3. Ejecución del Análisis Estático en SonarQube Local
Teniendo una instancia de SonarQube activa en `http://localhost:9000`:
```bash
mvn sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=<TU_TOKEN_DE_AUTENTICACION>
```

---

## 6. Conclusiones Profesionales sobre Métricas y Arquitectura

1. **La Complejidad Ciclomática como Brújula de Refactorización:**  
   La Complejidad Ciclomática no es una métrica burocrática; es un indicador directo del esfuerzo cognitivo y la dificultad de probar un componente. Un método con $\text{CC} = 20$ requiere como mínimo 20 caminos de prueba para aspirar a una cobertura de ramas completa, convirtiéndose en un foco de errores ante futuros requerimientos.
2. **Impacto de los Patrones de Diseño en la Mantenibilidad:**  
   La aplicación del **Patrón Strategy** permitió adherirse estrictamente al **Principio de Responsabilidad Única (SRP)** y al **Principio de Abierto/Cerrado (OCP)** de SOLID. Para incorporar una nueva modalidad de envío (por ejemplo, `EnvioMaritimoStrategy`), no es necesario modificar condicionales existentes ni arriesgar el funcionamiento de las modalidades en producción; basta con crear una nueva clase que implemente `EstrategiaEnvio` y registrarla en el orquestador.
3. **Calidad Guiada por Pruebas Inteligentes:**  
   Aumentar la cobertura no consiste en escribir aserciones vacías para complacer un Quality Gate. La combinación de particiones de equivalencia, análisis de valores frontera y pruebas parametrizadas en JUnit 5 permitió alcanzar un 98.2% de cobertura real y representativa, blindando el servicio contra regresiones operativas en producción.
