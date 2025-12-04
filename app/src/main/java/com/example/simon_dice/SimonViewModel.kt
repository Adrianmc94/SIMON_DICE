package com.example.simon_dice

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.simon_dice.data.RecordRepository
import com.example.simon_dice.model.Record

/**
 * ViewModel para gestionar la lógica del juego Simón Dice y la persistencia del récord.
 *
 * Referencia: [ViewModel | Android Developers](https://developer.android.com/topic/libraries/architecture/viewmodel)
 * El ViewModel sobrevive a los cambios de configuración y gestiona la comunicación
 * entre la UI y la capa de Datos (Repository).
 *
 * @property recordRepository El repositorio para acceder a los datos del récord.
 */
class SimonViewModel(private val recordRepository: RecordRepository) : ViewModel() {

    // ===============================================
    // Lógica del Récord (NUEVO)
    // ===============================================

    // LiveData que expone el récord actual a la UI
    private val _record = MutableLiveData<Record>()
    val record: LiveData<Record> = _record

    // LiveData para el nivel actual (Asumimos que ya existe)
    private val _currentLevel = MutableLiveData(0)
    val currentLevel: LiveData<Int> = _currentLevel

    // ... Otras variables del juego (secuencia, estado, etc.) ...

    init {
        // Carga el récord existente al iniciar el ViewModel
        loadRecord()
    }

    /**
     * Carga el récord actual desde el repositorio y actualiza el LiveData.
     */
    private fun loadRecord() {
        _record.value = recordRepository.getRecord()
    }

    /**
     * Función llamada cuando el jugador pierde (Game Over).
     * Comprueba si el nivel alcanzado supera el récord y lo guarda.
     * * @param nivelAnterior El último nivel superado (o el nivel actual - 1).
     */
    fun handleGameOver(nivelAnterior: Int) {
        // ... (Lógica de Game Over: Sonido de error, cambio de estado, etc.) ...

        // Guardar récord
        checkAndSaveRecord(nivelAnterior)
    }

    /**
     * Llama al repositorio para intentar guardar el nivel alcanzado.
     * Si se guarda un nuevo récord, actualiza el LiveData.
     * * @param nivelAlcanzado El nivel que intentamos establecer como nuevo récord.
     */
    private fun checkAndSaveRecord(nivelAlcanzado: Int) {
        val isNewRecord = recordRepository.saveIfNewRecord(nivelAlcanzado)
        // Si el repositorio confirma que hubo un nuevo récord, actualizamos la UI
        if (isNewRecord) {
            loadRecord()
            // Podrías añadir lógica aquí para mostrar un mensaje de "¡NUEVO RÉCORD!"
        }
    }

    // ===============================================
    // Lógica del Juego (Ejemplo)
    // ===============================================

    /**
     * Lógica para avanzar al siguiente nivel.
     */
    fun nextLevel() {
        val next = (_currentLevel.value ?: 0) + 1
        _currentLevel.value = next
        // ... (Lógica de Simón: Añadir color, reproducir secuencia) ...
    }

    // ... Resto de la lógica del juego (start, onPlayerClick, verificar secuencia, etc.) ...
}