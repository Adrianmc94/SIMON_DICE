package com.example.simon_dice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simon_dice.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

// MODELO DE ESTADOS Y DATOS
sealed class GameState {
    data object INICIO : GameState() // Esperando clic en "Start"
    data object SIMON_TURNO : GameState() // Simón genera y muestra la secuencia
    data object JUGADOR_TURNO : GameState() // Jugador introduce su secuencia
    data object GAME_OVER : GameState() // Juego terminado por fallo
}
enum class ColorJuego(val id: Int, val colorRes: Int, val tonoRes: Int) {
    VERDE(0, R.color.simon_green, R.raw.tono_verde),
    ROJO(1, R.color.simon_red, R.raw.tono_rojo),
    AZUL(2, R.color.simon_blue, R.raw.tono_azul),
    AMARILLO(3, R.color.simon_yellow, R.raw.tono_amarillo);

    companion object {
        fun fromId(id: Int) = entries.first { it.id == id }
    }
}

class SimonViewModel : ViewModel() {

    // ESTADOS OBSERVABLES (StateFlows)

    // Estado principal del juego
    private val _gameState = MutableStateFlow<GameState>(GameState.INICIO)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    // Secuencia que Simón ha generado (lista de IDs de ColorJuego)
    private val _simonSequence = MutableStateFlow<List<Int>>(emptyList())
    // Expuesto como inmutable para el UI
    val simonSequence: StateFlow<List<Int>> = _simonSequence.asStateFlow()

    // Input actual del jugador (lista de IDs de ColorJuego)
    private val _playerInput = MutableStateFlow<List<Int>>(emptyList())
    val playerInput: StateFlow<List<Int>> = _playerInput.asStateFlow()

    // Nivel actual
    private val _level = MutableStateFlow(1)
    val level: StateFlow<Int> = _level.asStateFlow()

    // Feedback visual: ID del color que debe estar iluminado en un momento dado
    private val _feedbackColor = MutableStateFlow<Int?>(null)
    val feedbackColor: StateFlow<Int?> = _feedbackColor.asStateFlow()

    // Job para la reproducción de la secuencia (permite cancelación)
    private var simonJob: Job? = null


    // FUNCIONES DE LÓGICA DE JUEGO

    /**
     * Inicia una nueva partida, reseteando las variables clave.
     * Llamado desde INICIO o GAME_OVER.
     */
    fun iniciarJuego() {
        simonJob?.cancel() // Cancela cualquier secuencia anterior
        _simonSequence.value = emptyList()
        _playerInput.value = emptyList()
        _level.value = 1
        ejecutarTurnoSimon() // Pasa directamente al turno de Simón
    }

    /**
     * Fase 2: Simón añade un nuevo color y reproduce la secuencia completa.
     */
    private fun ejecutarTurnoSimon() {
        _gameState.value = GameState.SIMON_TURNO // Cambia el estado
        _playerInput.value = emptyList() // Resetea el input del jugador

        // Añade un nuevo color aleatorio a la secuencia
        val nuevoColorId = Random.nextInt(4)
        val nuevaSecuencia = _simonSequence.value + nuevoColorId
        _simonSequence.value = nuevaSecuencia

        // Reproducir la secuencia de forma asíncrona (corrutinas)
        simonJob = viewModelScope.launch {
            delay(1000L) // Pausa inicial antes de empezar

            for (colorId in _simonSequence.value) {
                // Iluminar y Sonar
                _feedbackColor.value = colorId
                delay(500L) // Tiempo de iluminación/sonido (500 ms)

                // Pausa entre colores
                _feedbackColor.value = null // Apagar
                delay(250L) // Pausa entre elementos (250 ms)
            }

            // Fin de Reproducción: Pasar al turno del jugador
            _gameState.value = GameState.JUGADOR_TURNO
        }
    }

    /**
     * Fase 3: Maneja el clic de un botón de color por parte del jugador.
     * @param colorId ID del color clicado (0, 1, 2, 3).
     */
    fun manejarClickJugador(colorId: Int) {
        // Ignorar clics si no es el turno del jugador
        if (_gameState.value != GameState.JUGADOR_TURNO) return

        // Dar feedback inmediato (iluminar botón)
        viewModelScope.launch {
            _feedbackColor.value = colorId
            delay(100L) // Feedback breve
            _feedbackColor.value = null
        }

        // Agregar el input a la lista
        val nuevoInput = _playerInput.value + colorId
        _playerInput.value = nuevoInput

        // Verificación
        val index = nuevoInput.lastIndex // Posición del último clic
        val colorEsperado = _simonSequence.value[index]

        if (colorId == colorEsperado) {
            // Acierto parcial
            if (nuevoInput.size == _simonSequence.value.size) {
                // Acierto de ronda completa
                viewModelScope.launch {
                    // Muestra mensaje de exito y espera
                    delay(500L)

                    // Completar la Ronda
                    _level.value++ // Incrementa el nivel
                    ejecutarTurnoSimon() // Regresa a la Fase 2 (siguiente ronda)
                }
            }
            // Si no es el último, simplemente espera el siguiente clic
        } else {
            // Condición de Derrota (Game Over)
            _gameState.value = GameState.GAME_OVER
        }
    }

    // Función para tests
    fun setGameState(state: GameState) {
        _gameState.value = state
    }
}