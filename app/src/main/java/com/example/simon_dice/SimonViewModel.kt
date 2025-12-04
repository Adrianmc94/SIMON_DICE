package com.example.simon_dice

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simon_dice.data.RecordRepository
import com.example.simon_dice.model.Record
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// Representa los estados clave del juego.
enum class GameState {
    INICIO,
    SIMON_TURNO,
    JUGADOR_TURNO,
    GAME_OVER
}

// Representa los colores del juego.
enum class SimonColor { GREEN, RED, BLUE, YELLOW }

/**
 * ViewModel que gestiona la lógica del juego y la persistencia del récord.
 * Se inyecta el repositorio para desacoplar la capa de datos.
 *
 * Referencia: [ViewModel | Android Developers](https://developer.android.com/topic/libraries/architecture/viewmodel)
 */
class SimonViewModel(private val recordRepository: RecordRepository) : ViewModel() {

    // Live Data expuesto a la UI
    private val _record = MutableLiveData<Record>()
    val record: LiveData<Record> = _record

    private val _currentLevel = MutableLiveData(0)
    val currentLevel: LiveData<Int> = _currentLevel

    private val _gameState = MutableLiveData(GameState.INICIO)
    val gameState: LiveData<GameState> = _gameState

    private val _colorToFlash = MutableLiveData<SimonColor?>()
    val colorToFlash: LiveData<SimonColor?> = _colorToFlash

    // Lógica interna
    private var simonSequence = mutableListOf<SimonColor>()
    private var playerSequenceIndex: Int = 0

    init {
        // Carga el récord al inicio del ViewModel.
        loadRecord()
    }

    /**
     * Inicia una nueva partida, reiniciando secuencia y nivel.
     * Referencia: [ViewModel Lifecycle | Android Developers]
     */
    fun startGame() {
        simonSequence.clear()
        playerSequenceIndex = 0
        _currentLevel.value = 0
        nextLevel()
    }

    /**
     * Avanza al siguiente nivel, añade un color aleatorio y activa el turno de Simón.
     * Referencia: [State management in Compose | Android Developers] (Aplicable al cambio de estados)
     */
    fun nextLevel() {
        _currentLevel.value = (_currentLevel.value ?: 0) + 1
        _gameState.value = GameState.SIMON_TURNO

        val nextColor = SimonColor.entries[Random.nextInt(SimonColor.entries.size)]
        simonSequence.add(nextColor)

        // Lanzamos la secuencia en una corrutina para evitar bloquear la UI.
        viewModelScope.launch {
            playSimonSequence()
        }
    }

    /**
     * Reproduce la secuencia de colores de Simón con pausas.
     * Referencia: [Coroutines on Android | Android Developers] (Uso de viewModelScope y delay)
     */
    private suspend fun playSimonSequence() {
        delay(500L)
        for (color in simonSequence) {
            _colorToFlash.value = color
            delay(500L)
            _colorToFlash.value = null
            delay(250L)
        }

        _gameState.value = GameState.JUGADOR_TURNO
        playerSequenceIndex = 0
    }

    /**
     * Procesa el click del jugador, verificando si coincide con la secuencia.
     * @param clickedColor El color seleccionado por el jugador.
     */
    fun onPlayerClick(clickedColor: SimonColor) {
        if (_gameState.value != GameState.JUGADOR_TURNO) return

        val expectedColor = simonSequence[playerSequenceIndex]

        if (clickedColor == expectedColor) {
            viewModelScope.launch {
                // Flash rápido para feedback de acierto
                _colorToFlash.value = clickedColor
                delay(100L)
                _colorToFlash.value = null
            }

            playerSequenceIndex++

            if (playerSequenceIndex == simonSequence.size) {
                // Secuencia completada, avanza de nivel.
                viewModelScope.launch {
                    delay(800L)
                    nextLevel()
                }
            }
        } else {
            // Input incorrecto: Game Over.
            handleGameOver(_currentLevel.value ?: 0)
        }
    }

    /**
     * Maneja el fin de la partida. Establece el estado GAME_OVER y verifica si se ha batido el récord.
     * Referencia: [ViewModel and Data Persistence | Android Developers]
     * @param nivelAnterior El último nivel superado.
     */
    fun handleGameOver(nivelAnterior: Int) {
        _gameState.value = GameState.GAME_OVER
        checkAndSaveRecord(nivelAnterior)
    }

    /**
     * Carga el récord actual desde el repositorio.
     */
    private fun loadRecord() {
        _record.value = recordRepository.getRecord()
    }

    /**
     * Intenta guardar el nivel alcanzado como nuevo récord.
     * Si se guarda, actualiza el LiveData.
     */
    private fun checkAndSaveRecord(nivelAlcanzado: Int) {
        val isNewRecord = recordRepository.saveIfNewRecord(nivelAlcanzado)
        if (isNewRecord) {
            loadRecord()
        }
    }
}