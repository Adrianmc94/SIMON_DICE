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
import android.graphics.drawable.ColorDrawable
import android.media.SoundPool // NUEVO
import android.media.AudioAttributes // NUEVO

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

    // Almacena los colores originales y poder restaurarlos después del parpadeo.
    private lateinit var coloresOriginales: Map<Int, Int>

    // Variables para el control de Sonido
    private lateinit var soundPool: SoundPool
    private val soundMap = mutableMapOf<Int, Int>()
    private var idTonoError: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        configurarSonidos() // Configura SoundPool
        configurarVistas()

        btnInicio.setOnClickListener {
            if (!esTurnoSimon) {
                iniciarJuego()
            }
        }
    }

    // Configura y carga los tonos del juego en SoundPool
    private fun configurarSonidos() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        // Asumiendo que tienes tono_verde, tono_rojo, tono_azul, tono_amarillo y tono_error en res/raw
        soundMap[0] = soundPool.load(this, R.raw.tono_verde, 1)
        soundMap[1] = soundPool.load(this, R.raw.tono_rojo, 1)
        soundMap[2] = soundPool.load(this, R.raw.tono_azul, 1)
        soundMap[3] = soundPool.load(this, R.raw.tono_amarillo, 1)

        idTonoError = soundPool.load(this, R.raw.tono_error, 1)
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

        // FIX APLICADO: Leemos el color base de los botones usando backgroundTintList.
        coloresOriginales = mapOf(
            0 to (btnVerde.backgroundTintList?.defaultColor ?: android.graphics.Color.GREEN),
            1 to (btnRojo.backgroundTintList?.defaultColor ?: android.graphics.Color.RED),
            2 to (btnAzul.backgroundTintList?.defaultColor ?: android.graphics.Color.BLUE),
            3 to (btnAmarillo.backgroundTintList?.defaultColor ?: android.graphics.Color.YELLOW)
        )

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

    // Función de sonido
    private fun reproducirTono(colorId: Int) {
        val soundId = soundMap[colorId]
        if (soundId != null) {
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        }
    }

    // Función de sonido de error
    private fun reproducirError() {
        soundPool.play(idTonoError, 1f, 1f, 1, 0, 1f)
    }

    // FUNCIÓN DE FEEDBACK VISUAL
    private suspend fun resaltarBoton(boton: Button, colorOriginal: Int) {
        boton.setBackgroundColor(android.graphics.Color.WHITE)

        delay(500L)

        // Restaurar el color original
        boton.setBackgroundColor(colorOriginal)
    }

    // Fase 1: Inicialización
    private fun iniciarJuego() {
        secuencia.clear()
        nivel = 0
        indiceSecuenciaJugador = 0
        btnInicio.text = "REINICIAR"
        btnInicio.isEnabled = false
        turnoSimon()
    }

    // Fase 2: Turno de Simón
    private fun turnoSimon() = ambitoJuego.launch {
        esTurnoSimon = true

        nivel++
        tvPuntuacion.text = "Nivel: $nivel"
        tvEstado.text = "Simón Muestra"

        val nuevoColor = (0..3).random()
        secuencia.add(nuevoColor)
        indiceSecuenciaJugador = 0

        deshabilitarBotones()

        delay(500L)

        // Reproducir Secuencia con Feedback Visual y Auditivo
        for (color in secuencia) {
            val botonActual = botonesColor[color]
            val colorOriginal = coloresOriginales[color] ?: android.graphics.Color.GRAY

            reproducirTono(color) //
            resaltarBoton(botonActual, colorOriginal)

            delay(250L) // Pausa entre tonos
        }

        turnoJugador()
    }

    // Fase 3: Transición al Turno del Jugador
    private fun turnoJugador() {
        esTurnoSimon = false
        tvEstado.text = "Tu Turno"
        habilitarBotones()
        Log.d("FlujoJuego", "Turno del Jugador. Esperando Input.")
    }

    // Fase 3: Verificación del Clic del Jugador
    private fun manejarInputJugador(colorInput: Int) {
        val colorEsperado = secuencia[indiceSecuenciaJugador]
        val botonClicado = botonesColor[colorInput]
        val colorOriginal = coloresOriginales[colorInput] ?: android.graphics.Color.GRAY

        // Ejecutar el resaltado y sonido de forma asíncrona
        ambitoJuego.launch {
            reproducirTono(colorInput) // LLAMADA AL SONIDO
            resaltarBoton(botonClicado, colorOriginal)
        }

        if (colorInput == colorEsperado) {
            indiceSecuenciaJugador++

            if (indiceSecuenciaJugador == secuencia.size) { // Secuencia completa
                Log.d("FlujoJuego", "Secuencia de nivel $nivel completada con éxito.")
                turnoSimon()
            } else {
                Log.d("FlujoJuego", "Acertado. Faltan ${secuencia.size - indiceSecuenciaJugador} clicks.")
            }
        } else {
            finalizarJuego()
        }
    }

    // Condición de Derrota
    private fun finalizarJuego() {
        reproducirError()

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
        soundPool.release() // Libera los recursos de sonido
    }

}