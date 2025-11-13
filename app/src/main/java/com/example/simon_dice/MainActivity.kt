package com.example.simon_dice

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.graphics.drawable.ColorDrawable
import android.media.SoundPool
import android.media.AudioAttributes
import androidx.activity.viewModels // <-- IMPORTANTE: para usar by viewModels()

class MainActivity : AppCompatActivity() {

    // VISTAS Y VIEW MODEL
    private lateinit var tvEstado: TextView
    private lateinit var tvPuntuacion: TextView
    private lateinit var btnInicio: Button
    private lateinit var botonesColor: List<Button>

    // Inicialización de ViewModel (la lógica)
    private val viewModel: SimonViewModel by viewModels()

    // COROUTINE SCOPE: Para manejar el parpadeo de forma asíncrona
    private val ambitoJuego = CoroutineScope(Dispatchers.Main)

    // Almacena los colores originales para restaurarlos después del parpadeo
    private lateinit var coloresOriginales: Map<Int, Int>

    // Variables para el control de Sonido
    private lateinit var soundPool: SoundPool
    private val soundMap = mutableMapOf<Int, Int>()
    private var idTonoError: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        configurarSonidos()
        configurarVistas()
        observarViewModel() // <-- CLAVE: Activa el patrón Observer
    }

    // --- CONFIGURACIÓN E INICIALIZACIÓN DE LA VISTA ---

    private fun configurarSonidos() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        // Carga de sonidos (IDs 0-3 para colores, idTonoError para el error)
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

        // Obtener los colores originales de los botones (FIX DE CASTING ANTERIOR)
        coloresOriginales = mapOf(
            0 to (btnVerde.backgroundTintList?.defaultColor ?: android.graphics.Color.GREEN),
            1 to (btnRojo.backgroundTintList?.defaultColor ?: android.graphics.Color.RED),
            2 to (btnAzul.backgroundTintList?.defaultColor ?: android.graphics.Color.BLUE),
            3 to (btnAmarillo.backgroundTintList?.defaultColor ?: android.graphics.Color.YELLOW)
        )

        // Configurar Listeners: solo llaman a funciones del ViewModel
        btnInicio.setOnClickListener {
            viewModel.iniciarJuego() // Llama a la lógica
        }

        botonesColor.forEachIndexed { indice, boton ->
            boton.setOnClickListener {
                viewModel.manejarInputJugador(indice) // Llama a la lógica
            }
        }
    }

    // --- IMPLEMENTACIÓN DEL PATRÓN OBSERVER ---

    private fun observarViewModel() {
        // Observa el estado del juego y lo muestra en el TextView
        viewModel.estadoJuego.observe(this) { estado ->
            tvEstado.text = estado
        }

        // Observa el nivel y lo muestra en el TextView
        viewModel.nivel.observe(this) { nivel ->
            tvPuntuacion.text = "Nivel: $nivel"
        }

        // Controla la habilitación de los botones de color
        viewModel.botonesHabilitados.observe(this) { habilitado ->
            if (habilitado) habilitarBotones() else deshabilitarBotones()
        }

        // Controla la habilitación del botón de inicio/reiniciar
        viewModel.botonInicioHabilitado.observe(this) { habilitado ->
            btnInicio.isEnabled = habilitado
            if (habilitado) btnInicio.text = "REINICIAR" else btnInicio.text = "INICIAR"
        }

        // Observador clave: Dispara la lógica de feedback UI/Audio
        viewModel.colorAParpadear.observe(this) { colorId ->
            if (colorId != null) {
                // Ejecuta la lógica de UI/Audio (que requiere Activity Context)
                val boton = botonesColor[colorId]
                val colorOriginal = coloresOriginales[colorId] ?: android.graphics.Color.GRAY

                ambitoJuego.launch {
                    reproducirTono(colorId)
                    resaltarBoton(boton, colorOriginal)
                }
            }
        }

        // Observa el evento de GAME_OVER para reproducir el sonido de error
        viewModel.debeSonarError.observe(this) { debeSonar ->
            if (debeSonar) reproducirError()
        }
    }

    // --- FUNCIONES DE FEEDBACK (PERMANECEN EN LA VIEW) ---

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

    // FUNCIÓN DE FEEDBACK VISUAL (requiere Coroutine)
    private suspend fun resaltarBoton(boton: Button, colorOriginal: Int) {
        boton.setBackgroundColor(android.graphics.Color.WHITE)

        delay(500L)

        // Restaurar el color original
        boton.setBackgroundColor(colorOriginal)
    }

    // Funciones Auxiliares de UI
    private fun deshabilitarBotones() {
        botonesColor.forEach { it.isEnabled = false }
    }

    private fun habilitarBotones() {
        botonesColor.forEach { it.isEnabled = true }
    }

    // --- LIMPIEZA DE RECURSOS ---

    override fun onDestroy() {
        super.onDestroy()
        ambitoJuego.cancel() // Cancelar coroutines de parpadeo
        soundPool.release() // Liberar recursos de sonido
    }
}