package com.example.simon_dice

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels // Para usar la delegación de ViewModels
import com.example.simon_dice.data.RecordRepository
import com.example.simon_dice.data.impl.RecordSharedPreferencesDataSource
import com.example.simon_dice.model.Record
import java.util.Observer // Usamos la importación completa para evitar conflictos

/**
 * Factory simple para crear el ViewModel con el Repositorio.
 * Necesario ya que el ViewModel tiene un argumento en el constructor.
 */
class SimonDiceViewModelFactory(
    private val repository: RecordRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SimonViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SimonViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


/**
 * Activity principal del juego Simón Dice.
 */
class MainActivity : AppCompatActivity() {

    // Instancia de los componentes de la capa de Datos (NUEVO)
    private lateinit var recordDataSource: RecordSharedPreferencesDataSource
    private lateinit var recordRepository: RecordRepository

    // Delegación del ViewModel usando el Factory (MEJOR PRÁCTICA)
    private val viewModel: SimonViewModel by viewModels {
        SimonDiceViewModelFactory(recordRepository)
    }

    private lateinit var tvScore: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvRecord: TextView // NUEVO TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Inicialización de la cadena de Dependencias (NUEVO)
        recordDataSource = RecordSharedPreferencesDataSource(applicationContext)
        recordRepository = RecordRepository(recordDataSource)

        // Inicialización de la UI
        tvScore = findViewById(R.id.tvScore)
        tvStatus = findViewById(R.id.tvStatus)
        tvRecord = findViewById(R.id.tvRecord) // Referencia al nuevo TextView

        setupObservers()
        setupListeners()
    }

    /**
     * Configura la observación de los LiveData del ViewModel.
     */
    private fun setupObservers() {
        // Observador del nivel actual (Asumimos que ya existía)
        viewModel.currentLevel.observe(this) { level ->
            tvScore.text = "Nivel: $level"
        }

        // Observador del Récord (NUEVO)
        // Referencia: [LiveData Overview | Android Developers](https://developer.android.com/topic/libraries/architecture/livedata)
        // Observa cambios en el récord para actualizar el TextView.
        viewModel.record.observe(this) { record: Record ->
            tvRecord.text = if (record.nivel > 0) {
                "Récord: Nivel ${record.nivel} (${record.marcaTiempo})"
            } else {
                "Récord: Nivel 0 (No establecido)"
            }
        }

        // ... Otros observadores (estado del juego, etc.) ...
    }

    /**
     * Configura los listeners del botón Start/Restart.
     */
    private fun setupListeners() {
        findViewById<android.widget.Button>(R.id.btnStartRestart).setOnClickListener {
            viewModel.nextLevel() // Ejemplo de cómo se inicia el juego
            tvStatus.text = "Simón Muestra..."
        }

        // ... Listeners para los botones de color ...
    }

    // Ejemplo de cómo llamar a la lógica de Game Over
    fun onGameLost(lastLevelAchieved: Int) {
        tvStatus.text = "¡Has Perdido!"
        // Llama al ViewModel para registrar el récord
        viewModel.handleGameOver(lastLevelAchieved)
    }
}