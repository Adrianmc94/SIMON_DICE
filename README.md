# PROYECTO SIMÓN DICE: FINALIZADO (v1.0)

Este proyecto implementa el clásico juego **Simón Dice** para **Android**, utilizando la arquitectura **Model-View-ViewModel (MVVM)** en **Kotlin**. La interfaz de usuario fue construida con **Jetpack Compose** y la gestión de versiones se realizó bajo la metodología **GitFlow**.

---

## 1. ARQUITECTURA (MVVM)

La implementación se basa en la separación de responsabilidades y la modularidad de la lógica:

* **ViewModel (`SimonViewModel.kt`):**
    * Contiene la lógica central del juego (gestión de secuencia, verificación de entrada y control de niveles).
    * Utiliza **`StateFlow`** para comunicar el estado reactivo del juego y **`SharedFlow`** para enviar eventos únicos (como la animación de color o el sonido de error) a la vista.
    * Emplea **Kotlin Coroutines** (dentro de `viewModelScope`) para manejar pausas y temporización de la secuencia de Simón sin bloquear el hilo principal.
    * Gestiona la persistencia de récords a través de un patrón Repository.

* **Modelo de Datos:**
    * `Record.kt`: Almacena la puntuación máxima y su fecha de obtención.
    * `RecordRepository.kt`: Centraliza la lógica de guardado y carga de récords.
    * `RecordDao.kt`: Utiliza **SharedPreferences** para la persistencia local de datos.

* **View (`MainActivity.kt`):**
    * Implementada íntegramente con **Jetpack Compose**.
    * Configura los componentes de la interfaz (botones de colores) y gestiona la reproducción de sonidos.
    * Observa el estado expuesto por el ViewModel para la correcta actualización de la interfaz.
    * Muestra el récord actual con su fecha correspondiente en pantalla.

---

## 2. SISTEMA DE RÉCORDS

El juego incluye un sistema completo y persistente para gestionar las puntuaciones máximas:

* **Persistencia Local:** Los récords se almacenan de forma automática y localmente usando **SharedPreferences**.
* **Validación:** Al finalizar una partida, la puntuación obtenida se compara con el récord existente.
* **Actualización:** El récord solo se actualiza si la nueva puntuación supera a la anterior.
* **Información Detallada:** Se guarda tanto la puntuación máxima como la fecha exacta en que fue conseguida.

---

## 3. PRUEBAS UNITARIAS

Se han desarrollado **pruebas unitarias** en `ExampleUnitTest.kt` para asegurar la robustez de la lógica del juego. Estas pruebas validan:

* La correcta **transición de estados** del ViewModel (INICIO, SIMON\_TURNO, JUGADOR\_TURNO, GAME\_OVER).
* La **temporización** y retrasos de la secuencia, críticos para la jugabilidad.
* La lógica de **avance de nivel** tras completar una secuencia múltiple con éxito.
* La **finalización del juego** (**GameOver**) y la emisión del evento de error al fallar la secuencia.
* El **sistema de récords**: su validación, guardado y la actualización de las puntuaciones máximas.

---

## 4. METODOLOGÍA GITFLOW

El proyecto se gestionó siguiendo la metodología **GitFlow**, empleando ramas permanentes para la estabilidad y ramas temporales para el desarrollo de funcionalidades:

### Ramas Permanentes
* `master`: Rama de **producción (Release)**. Contiene únicamente el código estable y etiquetado con versiones.
* `develop`: Rama de **integración**. Recibe todas las fusiones del trabajo proveniente de las ramas *feature*.

### Proceso de Desarrollo y Liberación (v1.0)
1.  **Desarrollo:** Cada componente o funcionalidad (UI, Lógica, MVVM, Sonido, Récords) se desarrolló en ramas `feature/` específicas.
2.  **Integración:** Una vez completada, cada rama `feature/` fue fusionada en la rama **`develop`**.
3.  **Liberación:** Cuando **`develop`** contenía la versión final y estable del juego, se fusionó en **`master`**.
4.  **Etiquetado:** La versión final se etiquetó como **`v1.0`** sobre la rama `master`.

---

## 5. VISTAS DEL PROYECTO

Se incluyen capturas de pantalla de la aplicación en ejecución.

<img width="639" height="893" alt="Captura de pantalla de la aplicación Simón Dice" src="https://github.com/user-attachments/assets/448cbcc1-ff86-4040-b99d-3a91483a8741" />

---

## 6. DIAGRAMAS

<img width="791" height="1122" alt="Diagrama de arquitectura del proyecto" src="https://github.com/user-attachments/assets/b59e48e5-7c18-4feb-aec1-560aa1385d28" />

<img width="784" height="1116" alt="Diagrama de flujo de datos y estados en el ViewModel" src="https://github.com/user-attachments/assets/addd6504-2f2b-4006-a9eb-694a04282f27" />
