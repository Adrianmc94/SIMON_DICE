# PROYECTO SIMÓN DICE: FINALIZADO

Este proyecto implementa el clásico juego Simón Dice en Android, utilizando la arquitectura **Model-View-ViewModel (MVVM)** en Kotlin. El control de versiones se manejó completamente bajo la metodología **GitFlow**.

## 1. ARQUITECTURA (MVVM)

La implementación se basa en la separación de lógica y la interfaz de usuario:

* **ViewModel (`SimonViewModel.kt`):**
    * Contiene la lógica del juego (secuencia, verificación y niveles).
    * Utiliza **`LiveData`** para comunicar el estado y los eventos de juego a la Activity.
    * Usa **Coroutines** (dentro de `viewModelScope`) para gestionar las pausas y el tiempo de la secuencia de Simón sin bloquear la interfaz.
* **View (`MainActivity.kt`):**
    * Se enfoca en la interfaz: configuración de botones, inicialización del sonido (`SoundPool`) y manejo de las Coroutines de parpadeo.
    * Observa el **`LiveData`** del ViewModel para actualizar el texto y reaccionar al parpadeo de colores y al sonido de error.
    * El ViewModel se inyecta fácilmente usando el delegado **`by viewModels()`**.

## 2. METODOLOGÍA GITFLOW

El proyecto se gestionó siguiendo GitFlow, utilizando ramas permanentes para la estabilidad y temporales para el desarrollo:

### Ramas Permanentes

* **`master`:** Rama de liberación (Release). Contiene únicamente el código estable y etiquetado con versiones.
* **`develop`:** Rama de integración. Aquí se fusiona todo el trabajo de las *features*.

### Proceso de Desarrollo y Liberación (v1.0)

1.  **Desarrollo:** Cada funcionalidad (UI, Lógica, MVVM, Sonido) se desarrolló en ramas `feature/` separadas.
2.  **Integración:** Cada `feature/` fue fusionada en la rama `develop` al finalizarse.
3.  **Liberación Final:** Una vez que `develop` contenía la versión final del juego, se fusionó en **`master`**.
4.  **Etiquetado:** La versión final se marcó con la etiqueta **`v1.0`** sobre la rama `master`.

## 3. VISTAS DEL PROYECTO

Aquí se pueden incluir capturas de pantalla de la aplicación en ejecución.

<img width="639" height="893" alt="image" src="https://github.com/user-attachments/assets/448cbcc1-ff86-4040-b99d-3a91483a8741" />

## 4. DIAGRAMAS:

<img width="791" height="1122" alt="image" src="https://github.com/user-attachments/assets/b59e48e5-7c18-4feb-aec1-560aa1385d28" />

<img width="784" height="1116" alt="image" src="https://github.com/user-attachments/assets/addd6504-2f2b-4006-a9eb-694a04282f27" />


