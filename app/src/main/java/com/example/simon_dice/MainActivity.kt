package com.example.simon_dice

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

class MainActivity : AppCompatActivity() {

    // Vistas de la IU (conectadas al activity_main.xml)
    private lateinit var tvStatus: TextView
    private lateinit var tvScore: TextView
    private lateinit var btnStart: Button
    private lateinit var colorButtons: List<Button>

    // Variables de Lógica del Juego (preparación para la siguiente feature)
    private val sequence = mutableListOf<Int>()
    private var level = 0
    private var playerSequenceIndex = 0
    private var isSimonTurn = false

    // Coroutine Scope necesario para la función simonTurn() futura
    private val gameScope = CoroutineScope(Dispatchers.Main)

    // Mapeo para simulación en Logcat (útil para la siguiente feature)
    private val colorMap = mapOf(
        0 to "VERDE", 1 to "ROJO",
        2 to "AZUL", 3 to "AMARILLO"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Inflar el layout XML

        setupViews()

        // Asignar Listener al botón de inicio (Estado INICIO)
        btnStart.setOnClickListener {
            if (!isSimonTurn) {
                startGame()
            }
        }
    }

    private fun setupViews() {
        // Conectar los TextViews y el botón principal
        tvStatus = findViewById(R.id.tvStatus)
        tvScore = findViewById(R.id.tvScore)
        btnStart = findViewById(R.id.btnStartRestart)

        // Conectar los 4 botones de color
        val btnGreen: Button = findViewById(R.id.btnGreen)
        val btnRed: Button = findViewById(R.id.btnRed)
        val btnBlue: Button = findViewById(R.id.btnBlue)
        val btnYellow: Button = findViewById(R.id.btnYellow)

        colorButtons = listOf(btnGreen, btnRed, btnBlue, btnYellow)

        // Configuración inicial de la IU
        tvScore.text = "Nivel: 0"
        tvStatus.text = "Pulsa START / RESTART"

        // Deshabilitar los botones de color inicialmente (parte del estado INICIO)
        disableButtons()
    }

    // Las funciones de lógica quedan en esqueleto, se completarán en la rama feature/logica-simon
    private fun startGame() {
        // Lógica de Inicializar Juego
        Log.d("GameFlow", "Inicializar Juego (Lógica pendiente)")
        tvStatus.text = "Juego Inicializado..."
    }

    private fun disableButtons() {
        colorButtons.forEach { it.isEnabled = false }
        btnStart.isEnabled = false
    }

    // Limpieza al destruir
    override fun onDestroy() {
        super.onDestroy()
        gameScope.cancel()
    }
}