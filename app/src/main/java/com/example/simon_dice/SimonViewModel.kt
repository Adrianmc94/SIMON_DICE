package com.example.simon_dice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simon_dice.data.Record
import com.example.simon_dice.data.RecordRepository
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

/**
 * ViewModel que gestiona la lógica del juego y el estado.
 * @property recordRepository Repositorio inyectado para la gestión del récord.
 */
class SimonViewModel(private val recordRepository: RecordRepository) : ViewModel() { // Constructor modificado para inyección

    // ESTADOS OBSERVABLES
    private val _gameState = MutableStateFlow<GameState>(GameState.INICIO)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _simonSequence = MutableStateFlow<List<Int>>(emptyList())
    val simonSequence: StateFlow<List<Int>> = _simonSequence.asStateFlow()

    private val _playerInput = MutableStateFlow<List<Int>>(emptyList())
    val playerInput: StateFlow<List<Int>> = _playerInput.asStateFlow()

    private val _level = MutableStateFlow(1)
    val level: StateFlow<Int> = _level.asStateFlow()

    private val _feedbackColor = MutableStateFlow<Int?>(null)
    val feedbackColor: StateFlow<Int?> = _feedbackColor.asStateFlow()

    // NUEVO ESTADO DEL RÉCORD
    private val _highRecord = MutableStateFlow(Record())
    val highRecord: StateFlow<Record> = _highRecord.asStateFlow()

    private var simonJob: Job? = null

    init {
        // Cargar el récord al inicializar el ViewModel
        loadRecord()
    }

    /**
     * Carga el récord actual desde el Repositorio.
     */
    private fun loadRecord() {
        viewModelScope.launch {
            _highRecord.value = recordRepository.getRecord()
        }
    }

    /**
     * Inicia una nueva partida, reseteando las variables y comenzando el turno de Simón.
     */
    fun iniciarJuego() {
        simonJob?.cancel()
        _simonSequence.value = emptyList()
        _playerInput.value = emptyList()
        _level.value = 1
        ejecutarTurnoSimon()
    }

    /**
     * Genera un nuevo color, lo añade a la secuencia y la reproduce.
     */
    private fun ejecutarTurnoSimon() {
        _gameState.value = GameState.SIMON_TURNO
        _playerInput.value = emptyList() // Limpiar input del jugador

        // Añadir nuevo color a la secuencia
        val nuevoColorId = Random.nextInt(ColorJuego.entries.size)
        val nuevaSecuencia = _simonSequence.value + nuevoColorId
        _simonSequence.value = nuevaSecuencia

        // Reproducir la secuencia
        simonJob = viewModelScope.launch {
            delay(1000L) // Pausa inicial

            for (colorId in nuevaSecuencia) {
                _feedbackColor.value = colorId // Muestra el color
                delay(500L) // Duración del flash
                _feedbackColor.value = null
                delay(100L) // Pausa entre colores
            }

            // Pasar al turno del jugador
            _gameState.value = GameState.JUGADOR_TURNO
        }
    }

    /**
     * Maneja el clic de un botón de color por parte del jugador.
     */
    fun manejarClickJugador(colorId: Int) {
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
                    delay(500L) // Esperar antes de la siguiente ronda

                    // Completar la Ronda
                    _level.value++ // Incrementa el nivel
                    ejecutarTurnoSimon() // Regresa a la Fase 2 (siguiente ronda)
                }
            }
        } else {
            // Condición de Derrota (Game Over)
            _gameState.value = GameState.GAME_OVER
            // LÓGICA DE GUARDADO DEL RÉCORD
            saveHighRecordIfNew()
        }
    }

    /**
     * Comprueba si el nivel alcanzado es un nuevo récord y, si lo es, lo guarda.
     */
    private fun saveHighRecordIfNew() {
        viewModelScope.launch {
            val finalLevel = _level.value

            // Llamamos al Repositorio para la lógica de guardado
            val newRecordSet = recordRepository.updateRecordIfHigher(finalLevel, _highRecord.value)

            // Si se ha guardado un nuevo récord, actualizamos el StateFlow
            if (newRecordSet) {
                _highRecord.value = recordRepository.getRecord()
            }
        }
    }

    // Función auxiliar para tests
    fun setGameState(state: GameState) {
        _gameState.value = state
    }
}