# PROYECTO SIMÓN DICE: FINALIZADO (v1.0)

Este proyecto implementa el clásico juego Simón Dice en Android, utilizando la arquitectura **Model-View-ViewModel (MVVM)** en Kotlin. La interfaz de usuario fue construida con **Jetpack Compose** y el control de versiones se manejó completamente bajo la metodología **GitFlow**.

## 1. ARQUITECTURA (MVVM)

La implementación se basa en la separación de lógica y la interfaz de usuario:

* **ViewModel (`SimonViewModel.kt`):**
    * Contiene la lógica central del juego (secuencia, verificación de input y niveles).
    * Utiliza **`StateFlow`** para comunicar el estado del juego y **`SharedFlow`** para enviar eventos únicos (como el parpadeo de color o el sonido de error) a la vista.
    * Usa **Kotlin Coroutines** (dentro de `viewModelScope`) para gestionar las pausas y la temporización de la secuencia de Simón sin bloquear la interfaz.
* **View (`MainActivity.kt`):**
    * Implementada totalmente con la interfaz moderna de **Jetpack Compose**.
    * Se enfoca en la interfaz, la configuración de botones y la inicialización del motor de sonido (`SoundPool`).
    * Observa el estado del ViewModel para actualizar la UI y reaccionar a los eventos de parpadeo de colores y el sonido de error.

## 2. PRUEBAS UNITARIAS

Se han incluido pruebas unitarias exhaustivas en `ExampleUnitTest.kt` para asegurar la robustez de la lógica del juego. Estas pruebas validan:

* La correcta **transición de estados** del ViewModel (Ready, SimonShowing, PlayerTurn).
* La **temporización** precisa de los eventos de *flash* y retrasos de la secuencia, esenciales para la jugabilidad.
* La lógica de **avance de nivel** tras acertar una secuencia de múltiples pasos.
* La correcta **finalización del juego** (`GameOver`) y la emisión del sonido de error al fallar.

## 3. METODOLOGÍA GITFLOW

El proyecto se gestionó siguiendo GitFlow, utilizando ramas permanentes para la estabilidad y temporales para el desarrollo:

### Ramas Permanentes

* **`master`:** Rama de liberación (Release). Contiene únicamente el código estable y etiquetado con versiones.
* **`develop`:** Rama de integración. Aquí se fusiona todo el trabajo de las *features*.

### Proceso de Desarrollo y Liberación (v1.0)

1.  **Desarrollo:** Cada funcionalidad (UI, Lógica, MVVM, Sonido) se desarrolló en ramas `feature/` separadas.
2.  **Integración:** Cada `feature/` fue fusionada en la rama `develop` al finalizarse.
3.  **Liberación Final:** Una vez que `develop` contenía la versión final del juego, se fusionó en **`master`**.
4.  **Etiquetado:** La versión final se marcó con la etiqueta **`v1.0`** sobre la rama `master`.

## 4. VISTAS DEL PROYECTO

Aquí se pueden incluir capturas de pantalla de la aplicación en ejecución.

<img width="639" height="893" alt="image" src="https://github.com/user-attachments/assets/448cbcc1-ff86-4040-b99d-3a91483a8741" />

## 5. DIAGRAMAS:

<img width="791" height="1122" alt="image" src="https://github.com/user-attachments/assets/b59e48e5-7c18-4feb-aec1-560aa1385d28" />

<img width="784" height="1116" alt="image" src="https://github.com/user-attachments/assets/addd6504-2f2b-4006-a9eb-694a04282f27" />
