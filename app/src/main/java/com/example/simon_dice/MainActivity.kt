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

    // Vistas de la Interfaz de Usuario
    private lateinit var tvEstado: TextView
    private lateinit var tvPuntuacion: TextView
    private lateinit var btnInicio: Button
    private lateinit var botonesColor: List<Button>

    // Variables de Lógica del Juego
    private val secuencia = mutableListOf<Int>()
    private var nivel = 0
    private var indiceSecuenciaJugador = 0
    private var esTurnoSimon = false

    private val ambitoJuego = CoroutineScope(Dispatchers.Main)

    private val mapaColor = mapOf(
        0 to "VERDE", 1 to "ROJO",
        2 to "AZUL", 3 to "AMARILLO"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        configurarVistas()

        btnInicio.setOnClickListener {
            if (!esTurnoSimon) {
                iniciarJuego()
            }
        }
    }

    private fun configurarVistas() {
        tvEstado = findViewById(R.id.tvStatus)
        tvPuntuacion = findViewById(R.id.tvScore)
        btnInicio = findViewById(R.id.btnStartRestart)

        val btnVerde: Button = findViewById(R.id.btnGreen)
        val btnRojo: Button = findViewById(R.id.btnRed)
        val btnAzul: Button = findViewById(R.id.btnBlue)
        val btnAmarillo: Button = findViewById(R.id.btnYellow)
        botonesColor = listOf(btnVerde, btnRojo, btnAzul, btnAmarillo)

        // Configurar Listeners para los botones de color
        botonesColor.forEachIndexed { indice, boton ->
            boton.setOnClickListener {
                if (!esTurnoSimon) {
                    manejarInputJugador(indice)
                }
            }
        }

        tvPuntuacion.text = "Nivel: 0"
        tvEstado.text = "Pulsa INICIO / REINICIAR"
        deshabilitarBotones()
        btnInicio.isEnabled = true
    }

    // Fase 1: Inicialización
    private fun iniciarJuego() {
        secuencia.clear()
        nivel = 0
        indiceSecuenciaJugador = 0
        btnInicio.text = "REINICIAR"
        btnInicio.isEnabled = false // Se deshabilita mientras Simón juega
        turnoSimon()
    }

    // Fase 2: Turno de Simón (Reproducir Secuencia)
    private fun turnoSimon() = ambitoJuego.launch {
        esTurnoSimon = true

        nivel++
        tvPuntuacion.text = "Nivel: $nivel"
        tvEstado.text = "Simón Muestra"

        val nuevoColor = (0..3).random()
        secuencia.add(nuevoColor)
        indiceSecuenciaJugador = 0

        deshabilitarBotones() // Deshabilitar Entrada del Jugador

        delay(500L)

        // Reproducir Secuencia (Simulación en Logcat)
        for (color in secuencia) {
            val nombreColor = mapaColor[color] ?: "ERROR"
            Log.d("SIMON", "Reproduciendo: $nombreColor")
            delay(500L) // Duración del tono
            delay(250L) // Pausa entre tonos
        }

        turnoJugador()
    }

    // Fase 3: Transición al Turno del Jugador
    private fun turnoJugador() {
        esTurnoSimon = false
        tvEstado.text = "Tu Turno"
        habilitarBotones() // Habilitar Entrada del Jugador
        Log.d("FlujoJuego", "Turno del Jugador. Esperando Input.")
    }

    // Fase 3: Verificación del Clic del Jugador
    private fun manejarInputJugador(colorInput: Int) {
        val colorEsperado = secuencia[indiceSecuenciaJugador]
        val nombreColor = mapaColor[colorInput] ?: "ERROR"

        // Feedback inmediato (Simulación)
        Log.d("JUGADOR", "Input: $nombreColor. Índice: $indiceSecuenciaJugador")

        if (colorInput == colorEsperado) {
            indiceSecuenciaJugador++

            if (indiceSecuenciaJugador == secuencia.size) { // Secuencia completa
                Log.d("FlujoJuego", "Secuencia de nivel $nivel completada con éxito.")
                turnoSimon() // Pasa al siguiente nivel
            } else {
                Log.d("FlujoJuego", "Acertado. Faltan ${secuencia.size - indiceSecuenciaJugador} clicks.")
            }
        } else {
            finalizarJuego()
        }
    }

    // Condición de Derrota
    private fun finalizarJuego() {
        // Reproducir Sonido de Error (Simulación)
        Log.e("FlujoJuego", "¡JUEGO TERMINADO! Input incorrecto.")

        tvEstado.text = "¡Has Perdido! Nivel Alcanzado: $nivel"
        btnInicio.text = "REINICIAR"
        btnInicio.isEnabled = true

        deshabilitarBotones()
    }

    // Funciones Auxiliares
    private fun deshabilitarBotones() {
        botonesColor.forEach { it.isEnabled = false }
    }

    private fun habilitarBotones() {
        botonesColor.forEach { it.isEnabled = true }
    }

    override fun onDestroy() {
        super.onDestroy()
        ambitoJuego.cancel()
    }
}