package com.example.simon_dice

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random


class SimonViewModel : ViewModel() {

    // --- LIVE DATA: ESTADO OBSERVABLE ---
    // La Activity observará estos datos para actualizar la UI

    private val _estadoJuego = MutableLiveData("Pulsa INICIO / REINICIAR")
    val estadoJuego: LiveData<String> = _estadoJuego

    private val _nivel = MutableLiveData(0)
    val nivel: LiveData<Int> = _nivel

    // Este LiveData envía el ID del color que la Activity debe parpadear y sonar.
    // Usamos Int? y lo limpiamos (null) después de cada evento para que el Observer reaccione siempre.
    private val _colorAParpadear = MutableLiveData<Int?>(null)
    val colorAParpadear: LiveData<Int?> = _colorAParpadear

    // Este LiveData se activa a 'true' al perder. La Activity lo usará para sonar el error.
    private val _debeSonarError = MutableLiveData(false)
    val debeSonarError: LiveData<Boolean> = _debeSonarError

    // Este LiveData controla la habilitación de los botones de color.
    private val _botonesHabilitados = MutableLiveData(false)
    val botonesHabilitados: LiveData<Boolean> = _botonesHabilitados

    private val _botonInicioHabilitado = MutableLiveData(true)
    val botonInicioHabilitado: LiveData<Boolean> = _botonInicioHabilitado


    // --- LÓGICA INTERNA DEL JUEGO ---
    private val secuencia = mutableListOf<Int>()
    private var indiceSecuenciaJugador = 0
    private var esTurnoSimon = false

    // Función de Lógica: Fase 1: Inicialización
    fun iniciarJuego() {
        if (!esTurnoSimon) {
            secuencia.clear()
            _nivel.value = 0
            indiceSecuenciaJugador = 0
            _botonInicioHabilitado.value = false
            turnoSimon()
        }
    }

    // Función de Lógica: Fase 2: Turno de Simón
    private fun turnoSimon() = viewModelScope.launch {
        esTurnoSimon = true
        _botonesHabilitados.value = false // Deshabilitar botones mientras Simón muestra

        // Aumentar Nivel y actualizar UI
        _nivel.value = (_nivel.value ?: 0) + 1
        _estadoJuego.value = "Simón Muestra (Nivel ${_nivel.value})"

        // Añadir nuevo color
        val nuevoColor = Random.nextInt(0, 4) // Genera 0, 1, 2 o 3
        secuencia.add(nuevoColor)
        indiceSecuenciaJugador = 0

        delay(500L)

        // Reproducir Secuencia
        for (color in secuencia) {
            // Publicar el evento de parpadeo y sonido para que la Activity reaccione
            _colorAParpadear.value = color
            _colorAParpadear.value = null // Limpiar el evento para el siguiente parpadeo

            delay(750L) // Pausa entre tonos (500ms de parpadeo + 250ms de pausa)
        }

        turnoJugador()
    }

    // Función de Lógica: Fase 3: Transición al Turno del Jugador
    private fun turnoJugador() {
        esTurnoSimon = false
        _estadoJuego.value = "Tu Turno"
        _botonesHabilitados.value = true // Habilitar botones para el jugador
    }

    // Función de Lógica: Fase 3: Verificación del Clic del Jugador
    fun manejarInputJugador(colorInput: Int) {
        if (esTurnoSimon || _botonesHabilitados.value == false) return // Ignorar si no es el turno

        val colorEsperado = secuencia[indiceSecuenciaJugador]

        // Primero, enviar evento de feedback para el clic del jugador (parpadeo + sonido)
        _colorAParpadear.value = colorInput
        _colorAParpadear.value = null // Limpiar el evento

        if (colorInput == colorEsperado) {
            indiceSecuenciaJugador++

            if (indiceSecuenciaJugador == secuencia.size) { // Secuencia completa
                turnoSimon() // Pasar al siguiente nivel
            }
        } else {
            finalizarJuego()
        }
    }

    // Función de Lógica: Condición de Derrota (GAME_OVER)
    private fun finalizarJuego() {
        _debeSonarError.value = true // Activar evento de error para la Activity
        _debeSonarError.value = false // Limpiar el evento

        _estadoJuego.value = "¡Has Perdido! Nivel Alcanzado: ${_nivel.value}"
        _botonesHabilitados.value = false
        _botonInicioHabilitado.value = true
    }
}