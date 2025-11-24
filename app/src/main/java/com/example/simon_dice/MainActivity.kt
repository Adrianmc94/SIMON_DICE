package com.example.simon_dice

// Importamos lo necesario para sonidos, UI con Compose y corrutinas
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext

// Esta es la pantalla principal del juego
class MainActivity : ComponentActivity() {

    // Referencia al ViewModel para manejar la lógica del juego
    private val viewModel: SimonViewModel by viewModels()

    // Para manejar los sonidos del juego
    private lateinit var soundPool: SoundPool
    private val soundMap = mutableMapOf<Int, Int>() // mapas de sonidos por color
    private var idTonoError: Int = 0 // sonido de error

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializamos los sonidos
        initSoundPool()

        // Ponemos la UI usando Compose
        setContent {
            val uiState by viewModel.uiState.collectAsState() // estado actual del juego
            val flashing = remember { mutableStateMapOf<Int, Boolean>() } // para saber qué botón está parpadeando

            // Escuchamos eventos del juego (flash o sonido de error)
            LaunchedEffect(viewModel) {
                viewModel.eventFlow.collect { event ->
                    when (event) {
                        is UiEvent.FlashColor -> { // si toca parpadear un color
                            playToneForColor(event.colorId) // suena el tono
                            flashing[event.colorId] = true // activamos el flash
                            launch {
                                delay(event.duration) // esperamos un momento
                                flashing[event.colorId] = false // apagamos el flash
                            }
                        }
                        UiEvent.PlayErrorSound -> playErrorTone() // si hubo error, reproducimos tono de error
                    }
                }
            }

            // Llamamos a la función que dibuja la pantalla principal
            MainScreen(
                uiState = uiState,
                onStartClick = { viewModel.iniciarJuego() }, // iniciar juego
                onColorClick = { viewModel.manejarInputJugador(it) }, // cuando el jugador pulsa un color
                flashing = flashing
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release() // liberamos los sonidos al cerrar
    }

    // Preparamos todos los sonidos del juego
    private fun initSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        // Cargamos 4 tonos para los colores
        for (i in 0..3) {
            val resId = resources.getIdentifier("tone$i", "raw", packageName)
            if (resId != 0) soundMap[i] = soundPool.load(this, resId, 1)
        }

        // Cargamos el sonido de error
        val errorResId = resources.getIdentifier("error_tone", "raw", packageName)
        if (errorResId != 0) idTonoError = soundPool.load(this, errorResId, 1)
    }

    // Función para reproducir el sonido de un color
    private fun playToneForColor(colorId: Int) {
        soundMap[colorId]?.let { soundPool.play(it, 1f, 1f, 1, 0, 1f) }
    }

    // Función para reproducir el sonido de error
    private fun playErrorTone() {
        if (idTonoError != 0) soundPool.play(idTonoError, 1f, 1f, 1, 0, 1f)
    }
}

// ---------- Composables: UI de Compose ----------

// Esta función dibuja la pantalla principal del juego
@Composable
fun MainScreen(
    uiState: SimonState, // estado actual del juego
    onStartClick: () -> Unit, // acción al pulsar iniciar
    onColorClick: (Int) -> Unit, // acción al pulsar un color
    flashing: Map<Int, Boolean> // indica qué botones están parpadeando
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Mostramos nivel y mensaje
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Nivel: ${uiState.score}", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(text = uiState.state.statusText, fontSize = 18.sp)
        }

        // Botones de colores en 2 filas
        Column {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                ColorPadButton(0, uiState.isColorButtonsEnabled, flashing[0] == true) { onColorClick(0) }
                ColorPadButton(1, uiState.isColorButtonsEnabled, flashing[1] == true) { onColorClick(1) }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                ColorPadButton(2, uiState.isColorButtonsEnabled, flashing[2] == true) { onColorClick(2) }
                ColorPadButton(3, uiState.isColorButtonsEnabled, flashing[3] == true) { onColorClick(3) }
            }
        }

        // Botón de iniciar
        Button(
            onClick = onStartClick,
            enabled = uiState.isStartButtonEnabled,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("INICIAR")
        }
    }
}

// Este composable es un botón de color que puede parpadear
@Composable
fun ColorPadButton(index: Int, isEnabled: Boolean, isFlashing: Boolean, onClick: () -> Unit) {
    // Definimos los colores por índice
    val baseColor = when (index) {
        0 -> Color(0xFF4CAF50) // verde
        1 -> Color(0xFFF44336) // rojo
        2 -> Color(0xFFFFC107) // amarillo
        3 -> Color(0xFF2196F3) // azul
        else -> Color.Gray
    }

    // Si está parpadeando, mostramos blanco
    val displayColor = if (isFlashing) Color.White else baseColor

    Box(
        modifier = Modifier
            .size(140.dp)
            .then(if (isEnabled) Modifier.clickable { onClick() } else Modifier) // solo clicable si está activo
            .background(color = displayColor, shape = RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = " ", fontSize = 16.sp) // placeholder, no muestra texto
    }
}