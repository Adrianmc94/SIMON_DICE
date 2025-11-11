package com.example.simon_dice

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    // Vistas de la IU (conectadas al activity_main.xml)
    private lateinit var tvStatus: TextView
    private lateinit var tvScore: TextView
    private lateinit var btnStart: Button
    private lateinit var colorButtons: List<Button>

    // Variables de Lógica del Juego
    private val sequence = mutableListOf<Int>()
    private var level = 0
    private var playerSequenceIndex = 0
    private var isSimonTurn = false

    // Coroutine Scope necesario para la función simonTurn() futura
    private val gameScope = CoroutineScope(Dispatchers.Main)

    // Mapeo para simulación en Logcat
    private val colorMap = mapOf(
        0 to "VERDE", 1 to "ROJO",
        2 to "AZUL", 3 to "AMARILLO"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupViews()

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
        disableButtons()
    }

    // Lógica de Inicializar Juego
    private fun startGame() {
        sequence.clear()
        level = 0
        playerSequenceIndex = 0
        isSimonTurn = true

        btnStart.text = "REINICIAR"

        simonTurn()
    }

    // Lógica del Turno de Simón
    private fun simonTurn() = gameScope.launch {
        isSimonTurn = true

        level++
        tvScore.text = "Nivel: $level"
        tvStatus.text = "Simón Muestra"

        val newColor = (0..3).random()
        sequence.add(newColor)
        playerSequenceIndex = 0

        // Deshabilitar Entrada del Jugador
        disableButtons()

        delay(500L)

        // Reproducir Secuencia
        for (color in sequence) {
            val colorName = colorMap[color] ?: "ERROR"
            Log.d("SIMON", "Reproduciendo: $colorName")
            delay(500L)
            delay(250L) // Pausa entre tonos
        }

        playerTurn()
    }

    // Transición al Turno del Jugador
    private fun playerTurn() {
        isSimonTurn = false
        tvStatus.text = "Tu Turno"

        // Habilitar Entrada del Jugador
        enableButtons()

        Log.d("GameFlow", "Turno del Jugador. Esperando Input.")
    }

    // Funciones Auxiliares
    private fun disableButtons() {
        colorButtons.forEach { it.isEnabled = false }
        btnStart.isEnabled = false
    }

    private fun enableButtons() {
        colorButtons.forEach { it.isEnabled = true }
        btnStart.isEnabled = false
    }

    // Limpieza al destruir
    override fun onDestroy() {
        super.onDestroy()
        gameScope.cancel()
    }
}