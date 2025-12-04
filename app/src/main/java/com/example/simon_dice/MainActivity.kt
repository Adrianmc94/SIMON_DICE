package com.example.simon_dice

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.simon_dice.data.RecordRepository
import com.example.simon_dice.data.impl.RecordSharedPreferencesDataSource
import com.example.simon_dice.model.Record

/**
 * Factory personalizado para crear el ViewModel. Permite inyectar el [RecordRepository]
 * en el constructor del [SimonViewModel], siguiendo el principio SOLID.
 *
 * Referencia: [ViewModel Factory | Android Developers](https://developer.android.com/topic/libraries/architecture/viewmodel#vm-factory)
 */
class SimonDiceViewModelFactory(
    private val repository: RecordRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SimonViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SimonViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


/**
 * Activity principal del juego Simón Dice.
 * Actúa como la Vista (View) en el patrón MVVM, encargándose de la UI y los eventos.
 *
 * Referencia: [Activity | Android Developers](https://developer.android.com/reference/android/app/Activity)
 */
class MainActivity : AppCompatActivity() {

    /**
     * Fuente de Datos de Shared Preferences. Se usa 'lazy' para inicializarla solo
     * cuando sea requerida por el Repositorio.
     * Referencia: [Kotlin Lazy initialization | Kotlin Docs](https://kotlinlang.org/docs/delegated-properties.html#lazy-properties)
     */
    private val recordDataSource: RecordSharedPreferencesDataSource by lazy {
        RecordSharedPreferencesDataSource(applicationContext)
    }

    /**
     * Repositorio de Récord. Es la única fuente de datos conocida por el ViewModel.
     */
    private val recordRepository: RecordRepository by lazy {
        RecordRepository(recordDataSource)
    }

    /**
     * Delegación del ViewModel usando el Factory personalizado.
     * Referencia: [by viewModels | Android Developers](https://developer.android.com/topic/libraries/architecture/viewmodel#kotlin)
     */
    private val viewModel: SimonViewModel by viewModels {
        SimonDiceViewModelFactory(recordRepository)
    }

    // VISTAS
    private lateinit var tvScore: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvRecord: TextView

    // Botones del juego
    private lateinit var btnStartRestart: Button
    private lateinit var btnGreen: Button
    private lateinit var btnRed: Button
    private lateinit var btnBlue: Button
    private lateinit var btnYellow: Button

    /**
     * Punto de entrada de la Activity. Inicializa la UI y establece observadores y listeners.
     * Referencia: [Activity Lifecycle | Android Developers]
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar Vistas (Binding manual)
        tvScore = findViewById(R.id.tvScore)
        tvStatus = findViewById(R.id.tvStatus)
        tvRecord = findViewById(R.id.tvRecord)

        // Inicializar Botones
        btnStartRestart = findViewById(R.id.btnStartRestart)
        btnGreen = findViewById(R.id.btnGreen)
        btnRed = findViewById(R.id.btnRed)
        btnBlue = findViewById(R.id.btnBlue)
        btnYellow = findViewById(R.id.btnYellow)

        setupObservers()
        setupListeners()
    }

    /**
     * Configura la observación de los datos (LiveData) del ViewModel.
     */
    private fun setupObservers() {
        // Observador del nivel actual
        viewModel.currentLevel.observe(this) { level ->
            tvScore.text = "Nivel: $level"
        }

        // Observador del Récord (para actualizar el TextView del récord)
        // Referencia: [LiveData Overview | Android Developers](https://developer.android.com/topic/libraries/architecture/livedata)
        viewModel.record.observe(this) { record: Record ->
            tvRecord.text = if (record.nivel > 0) {
                "Récord: Nivel ${record.nivel} (${record.marcaTiempo})"
            } else {
                "Récord: Nivel 0 (No establecido)"
            }
        }
    }

    /**
     * Configura los listeners de los botones para enviar eventos al ViewModel.
     * Referencia: [View.setOnClickListener() | Android Developers]
     */
    private fun setupListeners() {
        // Listener del botón START / RESTART
        btnStartRestart.setOnClickListener {
            viewModel.startGame()
            tvStatus.text = "Simón Muestra..."
        }

        // Listeners para los botones de color (Envían la acción del jugador al ViewModel)
        btnGreen.setOnClickListener {
            viewModel.onPlayerClick(SimonColor.GREEN)
        }

        btnRed.setOnClickListener {
            viewModel.onPlayerClick(SimonColor.RED)
        }

        btnBlue.setOnClickListener {
            viewModel.onPlayerClick(SimonColor.BLUE)
        }

        btnYellow.setOnClickListener {
            viewModel.onPlayerClick(SimonColor.YELLOW)
        }
    }
}