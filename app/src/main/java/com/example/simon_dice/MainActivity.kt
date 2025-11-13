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
import android.media.SoundPool
import android.media.AudioAttributes
import androidx.activity.viewModels
import android.content.res.ColorStateList // Import necesario para ColorStateList

class MainActivity : AppCompatActivity() {

    // Vistas y ViewModel
    private lateinit var tvEstado: TextView
    private lateinit var tvPuntuacion: TextView
    private lateinit var btnInicio: Button
    private lateinit var botonesColor: List<Button>

    private val viewModel: SimonViewModel by viewModels()

    private val ambitoJuego = CoroutineScope(Dispatchers.Main)

    // Almacena el ColorStateList original para restaurar el tinte
    private lateinit var coloresOriginales: Map<Int, ColorStateList?>

    // Variables de sonido
    private lateinit var soundPool: SoundPool
    private val soundMap = mutableMapOf<Int, Int>()
    private var idTonoError: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        configurarSonidos()
        configurarVistas()
        observarViewModel()
    }

    private fun configurarSonidos() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        // Carga de sonidos
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

        // Almacenar el ColorStateList original de cada botón
        coloresOriginales = mapOf(
            0 to btnVerde.backgroundTintList,
            1 to btnRojo.backgroundTintList,
            2 to btnAzul.backgroundTintList,
            3 to btnAmarillo.backgroundTintList
        )

        btnInicio.setOnClickListener {
            viewModel.iniciarJuego()
        }

        botonesColor.forEachIndexed { indice, boton ->
            boton.setOnClickListener {
                viewModel.manejarInputJugador(indice)
            }
        }
    }

    private fun observarViewModel() {
        // Observa el estado del juego
        viewModel.estadoJuego.observe(this) { estado ->
            tvEstado.text = estado
        }

        // Observa el nivel
        viewModel.nivel.observe(this) { nivel ->
            tvPuntuacion.text = "Nivel: $nivel"
        }

        // Controla la habilitación de los botones
        viewModel.botonesHabilitados.observe(this) { habilitado ->
            if (habilitado) habilitarBotones() else deshabilitarBotones()
        }

        // Controla el botón de inicio
        viewModel.botonInicioHabilitado.observe(this) { habilitado ->
            btnInicio.isEnabled = habilitado
            if (habilitado) btnInicio.text = "REINICIAR" else btnInicio.text = "INICIAR"
        }

        // Dispara la acción de feedback (parpadeo y sonido)
        viewModel.colorAParpadear.observe(this) { colorId ->
            if (colorId != null) {
                val boton = botonesColor[colorId]
                val colorOriginalTintList = coloresOriginales[colorId]

                ambitoJuego.launch {
                    reproducirTono(colorId)
                    resaltarBoton(boton, colorOriginalTintList)
                }
            }
        }

        // Reproduce el sonido de error
        viewModel.debeSonarError.observe(this) { debeSonar ->
            if (debeSonar) reproducirError()
        }
    }

    private fun reproducirTono(colorId: Int) {
        val soundId = soundMap[colorId]
        if (soundId != null) {
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        }
    }

    private fun reproducirError() {
        soundPool.play(idTonoError, 1f, 1f, 1, 0, 1f)
    }

    private suspend fun resaltarBoton(boton: Button, colorOriginalTintList: ColorStateList?) {
        // Aplica tinte BLANCO para el parpadeo
        boton.backgroundTintList = ColorStateList.valueOf(android.graphics.Color.WHITE)

        delay(500L)

        // Restaura el ColorStateList original
        boton.backgroundTintList = colorOriginalTintList
    }

    private fun deshabilitarBotones() {
        botonesColor.forEach { it.isEnabled = false }
    }

    private fun habilitarBotones() {
        botonesColor.forEach { it.isEnabled = true }
    }

    override fun onDestroy() {
        super.onDestroy()
        ambitoJuego.cancel() // Limpia Coroutines
        soundPool.release() // Libera SoundPool
    }
}