package com.example.simon_dice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simon_dice.data.UserRecord
import com.example.simon_dice.data.RecordRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

sealed class GameState {
    data object INICIO : GameState()
    data object SIMON_TURNO : GameState()
    data object JUGADOR_TURNO : GameState()
    data object GAME_OVER : GameState()
}

enum class ColorJuego(val id: Int, val colorRes: Int, val tonoRes: Int) {
    VERDE(0, R.color.simon_green, R.raw.tono_verde),
    ROJO(1, R.color.simon_red, R.raw.tono_rojo),
    AZUL(2, R.color.simon_blue, R.raw.tono_azul),
    AMARILLO(3, R.color.simon_yellow, R.raw.tono_amarillo);
    companion object { fun fromId(id: Int) = entries.first { it.id == id } }
}

class SimonViewModel(private val recordRepository: RecordRepository) : ViewModel() {
    private val _gameState = MutableStateFlow<GameState>(GameState.INICIO)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _level = MutableStateFlow(0)
    val level: StateFlow<Int> = _level.asStateFlow()

    private val _simonSequence = MutableStateFlow<List<Int>>(emptyList())
    private val _playerInput = MutableStateFlow<List<Int>>(emptyList())
    private val _feedbackColor = MutableStateFlow<Int?>(null)
    val feedbackColor: StateFlow<Int?> = _feedbackColor.asStateFlow()

    private val _highRecord = MutableStateFlow(UserRecord(0, 0, 0L))
    val highRecord: StateFlow<UserRecord> = _highRecord.asStateFlow()

    private val _tonoASonar = MutableStateFlow<Int?>(null)
    val tonoASonar: StateFlow<Int?> = _tonoASonar.asStateFlow()

    private var simonTurnJob: Job? = null

    init { cargarRecord() }

    private fun cargarRecord() {
        viewModelScope.launch(Dispatchers.IO) {
            val record = recordRepository.getRecord()
            if (record != null) _highRecord.value = record
        }
    }

    fun iniciarJuego() {
        simonTurnJob?.cancel()
        _level.value = 1
        _simonSequence.value = emptyList()
        ejecutarTurnoSimon()
    }

    private fun ejecutarTurnoSimon() {
        _gameState.value = GameState.SIMON_TURNO
        _playerInput.value = emptyList()
        simonTurnJob = viewModelScope.launch {
            _simonSequence.value = _simonSequence.value + Random.nextInt(0, 4)
            delay(1000)
            for (id in _simonSequence.value) {
                _feedbackColor.value = id
                _tonoASonar.value = ColorJuego.fromId(id).tonoRes
                delay(500); _feedbackColor.value = null; delay(100)
            }
            _gameState.value = GameState.JUGADOR_TURNO
        }
    }

    fun manejarClickJugador(id: Int) {
        if (_gameState.value != GameState.JUGADOR_TURNO) return
        _feedbackColor.value = id
        _tonoASonar.value = ColorJuego.fromId(id).tonoRes
        viewModelScope.launch { delay(200); _feedbackColor.value = null }

        val nuevoInput = _playerInput.value + id
        _playerInput.value = nuevoInput
        if (id == _simonSequence.value[nuevoInput.lastIndex]) {
            if (nuevoInput.size == _simonSequence.value.size) {
                viewModelScope.launch { delay(500); _level.value++; ejecutarTurnoSimon() }
            }
        } else {
            _gameState.value = GameState.GAME_OVER
            saveHighRecordIfNew()
        }
    }

    private fun saveHighRecordIfNew() {
        viewModelScope.launch(Dispatchers.IO) {
            if (recordRepository.updateRecordIfHigher(_level.value, _highRecord.value)) {
                recordRepository.getRecord()?.let { _highRecord.value = it }
            }
        }
    }
    fun sonidoReproducido() { _tonoASonar.value = null }
}