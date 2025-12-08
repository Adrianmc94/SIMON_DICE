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

// --- CONSTANTES ---
// Duración de la iluminación del botón (simulando parpadeo/feedback)
private const val FLASH_DURATION = 500L
// Pausa entre la reproducción de colores
private const val SEQUENCE_DELAY = 100L
// Pausa al inicio de la secuencia de Simón (para que el jugador se prepare)
private const val INITIAL_DELAY = 1000L


// --- MODELO DE ESTADOS Y DATOS ---

sealed class GameState {
    data object INICIO : GameState() // Esperando clic en "Start"
    data object SIMON_TURNO : GameState() // Simón genera y muestra la secuencia
    data object JUGADOR_TURNO : GameState() // Jugador introduce su secuencia
    data object GAME_OVER : GameState() // Juego terminado por fallo
}

// Enum para representar los 4 colores (Mejora: Legibilidad y robustez)
// Usamos los IDs 0, 1, 2, 3 para que sean fáciles de indexar
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
 * [SimonViewModel] contiene toda la lógica de negocio del juego.
 * Gestiona el estado del juego y la interacción con el repositorio de récords.
 *
 * @property recordRepository El repositorio para la persistencia del récord.
 */
class SimonViewModel(
    private val recordRepository: RecordRepository = RecordRepository(object : RecordRepository.RecordDaoInterface {
        // Implementación dummy para el constructor por defecto sin contexto.
        // La implementación real se inyecta desde la Activity
        override fun loadRecord(): Record = Record()
        override fun saveRecord(score: Int, timestamp: Long) {}
    })
) : ViewModel() {

    // --- ESTADOS INTERNOS (MutableStateFlow) ---

    // Estado actual del juego (INICIO, SIMON_TURNO, JUGADOR_TURNO, GAME_OVER)
    private val _gameState = MutableStateFlow<GameState>(GameState.INICIO)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    // Nivel actual (puntuación)
    private val _level = MutableStateFlow(0)
    val level: StateFlow<Int> = _level.asStateFlow()

    // Secuencia de colores que Simón debe reproducir (IDs de ColorJuego)
    private val _simonSequence = MutableStateFlow<List<Int>>(emptyList())
    val simonSequence: StateFlow<List<Int>> = _simonSequence.asStateFlow()

    // Secuencia de inputs del jugador (IDs de ColorJuego)
    private val _playerInput = MutableStateFlow<List<Int>>(emptyList())
    val playerInput: StateFlow<List<Int>> = _playerInput.asStateFlow()

    // ID del color actualmente iluminado para el feedback visual (null si no hay)
    private val _feedbackColor = MutableStateFlow<Int?>(null)
    val feedbackColor: StateFlow<Int?> = _feedbackColor.asStateFlow()

    // El récord más alto (se carga al inicio)
    private val _highRecord = MutableStateFlow(Record())
    val highRecord: StateFlow<Record> = _highRecord.asStateFlow()


    // Evento para notificar a la UI que debe reproducir un sonido.
    // Usamos un StateFlow para el color, ya que el sonido (MediaPlayer) tiene dependencia del contexto de Android,
    // lo cual se gestiona en la UI.
    private val _tonoASonar = MutableStateFlow<Int?>(null)
    val tonoASonar: StateFlow<Int?> = _tonoASonar.asStateFlow()

    // Job para cancelar la reproducción de la secuencia si es necesario
    private var simonTurnJob: Job? = null


    // --- INICIALIZACIÓN ---

    init {
        // Cargar el récord al inicializar el ViewModel
        cargarRecord()
    }

    private fun cargarRecord() {
        viewModelScope.launch {
            _highRecord.value = recordRepository.getRecord()
        }
    }

    /**
     * Inicializa o reinicia el juego. Corresponde a la Fase 1.
     */
    fun iniciarJuego() {
        // Cancelar cualquier secuencia en curso si existe
        simonTurnJob?.cancel()
        // 1. Se inicializan variables clave
        _level.value = 1
        _simonSequence.value = emptyList()
        _playerInput.value = emptyList()
        _feedbackColor.value = null

        // 2. Se llama a la Fase 2 (Turno de Simón)
        ejecutarTurnoSimon()
    }

    /**
     * Ejecuta el turno de Simón. Corresponde a la Fase 2.
     */
    private fun ejecutarTurnoSimon() {
        // 1. Cambiar estado a SIMON_TURNO
        _gameState.value = GameState.SIMON_TURNO
        // Limpiar inputs del jugador para la nueva ronda
        _playerInput.value = emptyList()

        // El Job de Simón para la secuencia
        simonTurnJob = viewModelScope.launch {
            // A. Nivel + 1 y Mostrar: Se añade un nuevo color aleatorio
            agregarNuevoColor()

            // Pausa de preparación
            delay(INITIAL_DELAY)

            // B. Reproducción: Recorrer y reproducir la secuencia completa
            reproducirSecuencia(_simonSequence.value)

            // C. Fin de Reproducción: Se pasa a la Fase 3
            _gameState.value = GameState.JUGADOR_TURNO
        }
    }

    /**
     * Agrega un nuevo color aleatorio a la secuencia de Simón.
     */
    private fun agregarNuevoColor() {
        // Generar un número aleatorio entre 0 y 3 (los IDs de los colores)
        val nuevoColorId = Random.nextInt(0, 4)
        _simonSequence.value = _simonSequence.value + nuevoColorId
    }

    /**
     * Lanza una corrutina para simular la reproducción de la secuencia.
     * @param sequence La secuencia de IDs de color a reproducir.
     */
    private suspend fun reproducirSecuencia(sequence: List<Int>) {
        for (colorId in sequence) {
            // El botón se ilumina (feedback visual)
            _feedbackColor.value = colorId
            // Notificar a la UI que debe sonar el tono
            _tonoASonar.value = ColorJuego.fromId(colorId).tonoRes

            // El botón se ilumina por un periodo breve (e.g., 500 ms)
            delay(FLASH_DURATION)

            // El botón se apaga
            _feedbackColor.value = null

            // Hay una pequeña pausa entre la reproducción de cada color (e.g., 100 ms)
            delay(SEQUENCE_DELAY)
        }
    }

    /**
     * Gestiona el clic del jugador en un botón de color. Corresponde a la Fase 3.
     * @param colorId El ID del color pulsado (0-3).
     */
    fun manejarClickJugador(colorId: Int) {
        // Ignorar clics si no es el turno del jugador
        if (_gameState.value != GameState.JUGADOR_TURNO) return

        // 1. Feedback: Dar feedback inmediato (iluminar botón y sonar)
        viewModelScope.launch {
            _feedbackColor.value = colorId
            // Notificar a la UI que debe sonar el tono
            _tonoASonar.value = ColorJuego.fromId(colorId).tonoRes
            delay(100L) // Feedback breve
            _feedbackColor.value = null
        }

        // 2. Recepción de Clicks: Agregar el input a la lista
        val nuevoInput = _playerInput.value + colorId
        _playerInput.value = nuevoInput

        // 3. Verificación
        val index = nuevoInput.lastIndex // Posición del último clic
        val colorEsperado = _simonSequence.value.getOrNull(index)

        // Comprobamos si el clic es correcto
        if (colorId == colorEsperado) {
            // Acierto parcial
            if (nuevoInput.size == _simonSequence.value.size) {
                // Acierto de ronda completa
                viewModelScope.launch {
                    // Muestra mensaje de exito simulado con un delay antes de la siguiente ronda
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

    // Función para que la UI pueda confirmar que el sonido se ha reproducido
    fun sonidoReproducido() {
        _tonoASonar.value = null
    }

    // --- FUNCIONES PARA TESTS UNITARIOS ---
    // Estas funciones facilitan la manipulación del estado en los tests
    fun setLevel(newLevel: Int) { _level.value = newLevel }
    fun setSimonSequence(newSequence: List<Int>) { _simonSequence.value = newSequence }
    fun setPlayerInput(newInput: List<Int>) { _playerInput.value = newInput }
    fun setGameState(newState: GameState) { _gameState.value = newState }

    override fun onCleared() {
        super.onCleared()
        // No hay necesidad de limpiar corrutinas manualmente, viewModelScope lo hace
    }
}