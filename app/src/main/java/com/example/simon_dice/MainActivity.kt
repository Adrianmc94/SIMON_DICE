package com.example.simon_dice

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import android.text.format.DateFormat
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import com.example.simon_dice.data.RecordRepository
import com.example.simon_dice.data.local.RecordDao
import com.example.simon_dice.R
import java.util.Date
import java.util.Locale

/**
 * [SimonViewModelFactory] es una fábrica de ViewModels personalizada para inyectar dependencias.
 * @property repository El repositorio de datos que se inyectará.
 */
class SimonViewModelFactory(private val repository: RecordRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SimonViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SimonViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MainActivity : ComponentActivity() {

    // Inicializamos el Repositorio de Récords y el Factory para inyectar al ViewModel
    private lateinit var recordRepository: RecordRepository
    private lateinit var viewModelFactory: SimonViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inicialización de la capa de datos (DAOs y Repositorios)
        val recordDao = RecordDao(applicationContext)
        recordRepository = RecordRepository(recordDao)
        viewModelFactory = SimonViewModelFactory(recordRepository)

        setContent {
            // Definir un tema oscuro y fondo para mayor contraste de los botones
            Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF202020)) {
                // Inyectamos el ViewModel con la Factory
                SimonDiceScreen(viewModel(factory = viewModelFactory))
            }
        }
    }
}

// --- COMPONENTE PRINCIPAL (VISTA) ---
@Composable
fun SimonDiceScreen(viewModel: SimonViewModel = viewModel()) {
    // 1. Observar los estados del ViewModel (StateFlows)
    val gameState by viewModel.gameState.collectAsState()
    val level by viewModel.level.collectAsState()
    val feedbackColor by viewModel.feedbackColor.collectAsState()
    val tonoASonar by viewModel.tonoASonar.collectAsState()
    val highRecord by viewModel.highRecord.collectAsState()

    // 2. Lógica para reproducción de sonidos (Side Effect)
    val context = LocalContext.current

    // Usamos LaunchedEffect para manejar el evento de sonido una sola vez
    LaunchedEffect(tonoASonar) {
        val tonoId = tonoASonar
        if (tonoId != null) {
            // Utilizamos MediaPlayer para la reproducción, ya que es simple para un solo tono.
            // Se recomienda SoundPool para un juego más complejo y rápido, pero MediaPlayer es suficiente aquí.
            val mediaPlayer = MediaPlayer.create(context, tonoId)
            mediaPlayer.setOnCompletionListener { mp ->
                mp.release() // Liberar recursos
                viewModel.sonidoReproducido() // Notificar al VM que el sonido ha terminado
            }
            mediaPlayer.start()
        }
    }


    // 3. Renderizar la interfaz
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Marcadores y Estado
        HeaderSection(gameState, level, highRecord)

        // Los 4 Botones de Color (La Matriz 2x2)
        SimonButtonsMatrix(
            gameState = gameState,
            feedbackColor = feedbackColor,
            onButtonClick = viewModel::manejarClickJugador
        )

        // Botón de Inicio/Reinicio
        StartButton(
            gameState = gameState,
            onStartClick = viewModel::iniciarJuego
        )
    }
}


// --- COMPONENTES DE LA UI ---

@Composable
fun HeaderSection(gameState: GameState, level: Int, record: com.example.simon_dice.data.Record) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Nivel Actual
        Text(
            text = "Nivel: $level",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(top = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Mensaje de Estado
        val statusText = when (gameState) {
            GameState.INICIO -> "Pulsa INICIAR para empezar"
            GameState.SIMON_TURNO -> "Simón Muestra"
            GameState.JUGADOR_TURNO -> "¡Tu Turno! (Nivel ${level})"
            GameState.GAME_OVER -> "¡Juego Terminado! Nivel: ${level - 1}" // El nivel es el siguiente, el score es el anterior
        }
        val statusColor = if (gameState == GameState.GAME_OVER) Color.Red else Color.White
        Text(
            text = statusText,
            fontSize = 20.sp,
            color = statusColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Récord
        val recordText = if (record.score > 0) {
            // Formatear la marca de tiempo (Long) a fecha y hora legible
            val dateString = DateFormat.format("dd/MM/yyyy HH:mm", Date(record.timestamp)).toString()
            "Récord: ${record.score} (el $dateString)"
        } else {
            "Récord: 0"
        }
        Text(
            text = recordText,
            fontSize = 16.sp,
            color = Color.LightGray
        )
    }
}

@Composable
fun SimonButtonsMatrix(
    gameState: GameState,
    feedbackColor: Int?,
    onButtonClick: (Int) -> Unit
) {
    val colors = ColorJuego.entries // Todos los colores del enum
    // Los botones solo están habilitados si es el turno del jugador.
    val enabled = gameState == GameState.JUGADOR_TURNO

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Cuadrado perfecto para los botones
    ) {
        Row(modifier = Modifier.weight(1f)) {
            SimonButton(colors[0], enabled, feedbackColor, onButtonClick) // VERDE
            SimonButton(colors[1], enabled, feedbackColor, onButtonClick) // ROJO
        }
        Row(modifier = Modifier.weight(1f)) {
            SimonButton(colors[2], enabled, feedbackColor, onButtonClick) // AZUL
            SimonButton(colors[3], enabled, feedbackColor, onButtonClick) // AMARILLO
        }
    }
}

@Composable
fun RowScope.SimonButton(
    colorJuego: ColorJuego,
    enabled: Boolean,
    feedbackColor: Int?,
    onClick: (Int) -> Unit
) {
    // Verificar si este botón debe estar iluminado (simulando el parpadeo)
    val isIlluminated = feedbackColor == colorJuego.id

    // Usar colorResource para obtener el color desde R.color
    val baseColor = colorResource(id = colorJuego.colorRes)

    // Si está iluminado, usamos el color base (alpha 1f), si no, un color más oscuro (alpha 0.5f)
    val colorToUse = if (isIlluminated) baseColor.copy(alpha = 1f) else baseColor.copy(alpha = 0.5f)

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .padding(8.dp)
            .background(colorToUse, RoundedCornerShape(16.dp))
            // Deshabilita el clickable si no está enabled (esto es para el turno del jugador)
            .let {
                if (enabled) {
                    it.clickable { onClick(colorJuego.id) }
                } else {
                    it
                }
            }
    )
}

@Composable
fun StartButton(gameState: GameState, onStartClick: () -> Unit) {
    val buttonText = when (gameState) {
        GameState.INICIO -> "INICIAR JUEGO"
        GameState.GAME_OVER -> "REINICIAR"
        else -> "..." // En SIMON_TURNO y JUGADOR_TURNO, el botón está deshabilitado
    }

    // El botón solo está habilitado en INICIO o GAME_OVER
    val isEnabled = gameState == GameState.INICIO || gameState == GameState.GAME_OVER

    Button(
        onClick = onStartClick,
        enabled = isEnabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(top = 8.dp)
    ) {
        Text(buttonText, fontSize = 20.sp)
    }
}