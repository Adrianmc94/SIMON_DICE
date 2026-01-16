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

## BASE DE DATOS (ROOM)

Para evitar que el récord se borre al cerrar el juego, se ha sustituido el sistema de guardado simple por una base de datos **Room**, integrada directamente en el dispositivo.

### Guardado permanente
Las puntuaciones se almacenan en una base de datos local real, garantizando que los datos persistan incluso después de cerrar o reiniciar el juego, a diferencia de los archivos temporales.

### Organización de datos (UserData)
La información del jugador se guarda de forma estructurada mediante la entidad `UserData`, que incluye:
- Nivel alcanzado.
- Fecha exacta de la partida.

Esto facilita la gestión y el mantenimiento de los datos.

### Consultas rápidas
Se utiliza un **DAO (Data Access Object)** para realizar consultas eficientes, permitiendo obtener de forma rápida la puntuación más alta almacenada en la base de datos.

### Sin bloqueos
Las operaciones de guardado y consulta se ejecutan en segundo plano mediante **corrutinas**, evitando bloqueos en el hilo principal y asegurando que el juego no se congele durante el guardado de la partida.


**Triple Persistencia de datos**:
1.  **Room**: Almacenamiento local estructurado en SQLite para el historial de récords.
2.  **SharedPreferences**: Guardado rápido del récord máximo para persistencia inmediata.
3.  **MongoDB (Cloud)**: Sincronización simulada en la nube mediante una capa de servicio remoto.

*Arquitectura limpia con separación de responsabilidades mediante el patrón Repository.*


## 5. VISTAS DEL PROYECTO

Se incluyen capturas de pantalla de la aplicación en ejecución.

<img width="522" height="951" alt="Captura desde 2026-01-08 09-04-15" src="https://github.com/user-attachments/assets/3de8f947-6afd-4cb6-a260-899d95becd09" />


<img width="522" height="951" alt="Captura desde 2026-01-08 09-03-36" src="https://github.com/user-attachments/assets/ee97d47d-71b3-4bc7-a524-6ade4b367456" />

---

## 6. DIAGRAMAS

<img width="791" height="1122" alt="Diagrama de arquitectura del proyecto" src="https://github.com/user-attachments/assets/b59e48e5-7c18-4feb-aec1-560aa1385d28" />

<img width="784" height="1116" alt="Diagrama de flujo de datos y estados en el ViewModel" src="https://github.com/user-attachments/assets/addd6504-2f2b-4006-a9eb-694a04282f27" />
