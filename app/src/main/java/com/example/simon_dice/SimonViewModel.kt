package com.example.simon_dice

// Importamos lo necesario para manejar el estado del juego, corrutinas y flujos
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

// Representa un color del juego, solo con un ID
data class GameColor(val id: Int)

// Estado completo del juego
data class SimonState(
    val score: Int = 0, // nivel actual
    val state: GameState = GameState.Ready, // estado del juego
    val sequence: List<GameColor> = emptyList(), // secuencia que Simón muestra
    val playerInputs: List<GameColor> = emptyList(), // entradas del jugador
    val isStartButtonEnabled: Boolean = true, // si se puede pulsar iniciar
    val isColorButtonsEnabled: Boolean = false // si los botones de color están activos
)

// Diferentes estados posibles del juego
sealed class GameState(val statusText: String) {
    object Ready : GameState("Pulsa INICIAR")
    object SimonShowing : GameState("Simón Muestra")
    object PlayerTurn : GameState("¡Tu Turno!")
    object GameOver : GameState("¡Has Perdido!")
    object Won : GameState("¡Has Ganado!")
}

// Eventos que la UI puede recibir
sealed class UiEvent {
    data class FlashColor(val colorId: Int, val duration: Long) : UiEvent() // para parpadear un color
    object PlayErrorSound : UiEvent() // para reproducir el sonido de error
}

// ViewModel principal del juego
class SimonViewModel : ViewModel() {

    // Tiempos para los flashes y retrasos
    private val FLASH_DURATION_MS = 500L
    private val SEQUENCE_DELAY_MS = 100L
    private val INITIAL_DELAY_MS = 1000L
    private val R_DURATION_MS = FLASH_DURATION_MS + SEQUENCE_DELAY_MS

    // Estado actual del juego
    private val _uiState = MutableStateFlow(SimonState())
    val uiState: StateFlow<SimonState> = _uiState

    // Eventos que la UI escucha (un solo uso)
    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow: SharedFlow<UiEvent> = _eventFlow

    private var currentSimonJob: Job? = null
    private val secuencia: List<GameColor> get() = _uiState.value.sequence
    private val playerInputs: List<GameColor> get() = _uiState.value.playerInputs

    // Función para empezar un juego nuevo
    fun iniciarJuego() {
        if (_uiState.value.state == GameState.SimonShowing) return // si ya está mostrando, no hacer nada

        // Reiniciamos todo el estado
        _uiState.update {
            it.copy(
                score = 0,
                playerInputs = emptyList(),
                isStartButtonEnabled = false,
                isColorButtonsEnabled = false,
                state = GameState.Ready,
                sequence = emptyList()
            )
        }
        turnoSimon() // empieza el turno de Simón
    }

    // Turno de Simón: agrega un color y lo muestra
    private fun turnoSimon() {
        currentSimonJob?.cancel() // cancelamos cualquier secuencia anterior
        currentSimonJob = viewModelScope.launch {
            _uiState.update { it.copy(state = GameState.SimonShowing) } // cambiamos estado
            val nuevoNivel = _uiState.value.score + 1
            _uiState.update { it.copy(score = nuevoNivel) } // subimos el nivel

            // Añadimos un color aleatorio a la secuencia
            val nuevoColor = Random.nextInt(0, 4)
            val nuevaSecuencia = _uiState.value.sequence + GameColor(nuevoColor)
            _uiState.update { it.copy(sequence = nuevaSecuencia) }

            delay(INITIAL_DELAY_MS) // esperamos un poco antes de mostrar

            // Mandamos eventos de flash para cada color de la secuencia
            for (color in nuevaSecuencia) {
                _eventFlow.emit(UiEvent.FlashColor(color.id, FLASH_DURATION_MS))
                delay(R_DURATION_MS)
            }

            turnoJugador() // pasamos el turno al jugador
        }
    }

    // Activamos botones y cambiamos estado al turno del jugador
    private fun turnoJugador() {
        _uiState.update { it.copy(state = GameState.PlayerTurn, isColorButtonsEnabled = true) }
    }

    // Maneja lo que hace el jugador al pulsar un color
    fun manejarInputJugador(colorId: Int) {
        if (_uiState.value.state != GameState.PlayerTurn) return // solo se puede si es su turno

        // Mandamos un flash del color que pulsó
        viewModelScope.launch { _eventFlow.emit(UiEvent.FlashColor(colorId, FLASH_DURATION_MS)) }

        // Guardamos la entrada del jugador
        val nuevaPlayerInputs = playerInputs + GameColor(colorId)
        val indiceActual = nuevaPlayerInputs.size - 1
        _uiState.update { it.copy(playerInputs = nuevaPlayerInputs) }

        val colorEsperado = secuencia.getOrNull(indiceActual)

        if (colorEsperado != null && GameColor(colorId) == colorEsperado) {
            // Si acertó y terminó la secuencia, empezamos nuevo turno
            if (nuevaPlayerInputs.size == secuencia.size) {
                _uiState.update { it.copy(playerInputs = emptyList()) }
                turnoSimon()
            }
        } else finalizarJuego() // si se equivoca, termina el juego
    }

    // Termina el juego: desactiva botones y manda sonido de error
    private fun finalizarJuego() {
        viewModelScope.launch { _eventFlow.emit(UiEvent.PlayErrorSound) }
        _uiState.update {
            it.copy(state = GameState.GameOver, isColorButtonsEnabled = false, isStartButtonEnabled = true)
        }
    }
}