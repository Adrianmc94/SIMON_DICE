package com.example.simon_dice

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
import com.example.simon_dice.data.Record
import com.example.simon_dice.data.RecordRepository
import com.example.simon_dice.data.local.RecordDao
import com.example.simon_dice.R

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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicialización de la capa de datos
        val recordDao = RecordDao(applicationContext)
        val recordRepository = RecordRepository(recordDao)

        // Creación de la Factory
        val viewModelFactory = SimonViewModelFactory(recordRepository)

        setContent {
            // Se usa Surface directamente para establecer el color de fondo de la pantalla.
            Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF202020)) {
                // Usar la Factory para obtener el ViewModel
                SimonDiceScreen(viewModel = viewModel(factory = viewModelFactory))
            }
        }
    }
}

// COMPONENTE PRINCIPAL (VISTA)
@Composable
fun SimonDiceScreen(viewModel: SimonViewModel = viewModel()) {
    // 1. Observar los estados del ViewModel (StateFlows)
    val gameState by viewModel.gameState.collectAsState()
    val level by viewModel.level.collectAsState()
    val feedbackColor by viewModel.feedbackColor.collectAsState()
    val colors = ColorJuego.entries.toList()

    // NUEVO ESTADO: Observar el récord
    val highRecord by viewModel.highRecord.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        // Mostrar el Récord
        RecordDisplay(highRecord = highRecord)

        // Marcador y Mensaje de Estado
        ScoreAndStatus(gameState, level)

        // Botones de Simón (Grid)
        SimonButtonsGrid(
            colors = colors,
            enabled = gameState == GameState.JUGADOR_TURNO,
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

/**
 * Muestra el récord (score y marca de tiempo).
 */
@Composable
fun RecordDisplay(highRecord: Record) {
    // Formatear la marca de tiempo (dia y hora)
    val formattedTimestamp = if (highRecord.timestamp > 0) {
        DateFormat.format("dd/MM/yyyy HH:mm:ss", highRecord.timestamp).toString()
    } else {
        "Nunca"
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "RÉCORD: ${highRecord.score}",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFC107) // Amarillo para destacar
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Fecha: $formattedTimestamp",
            fontSize = 14.sp,
            color = Color.LightGray
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun ScoreAndStatus(gameState: GameState, level: Int) {
    val statusText = when (gameState) {
        GameState.INICIO -> "Pulsa INICIAR"
        GameState.SIMON_TURNO -> "Simón Muestra"
        GameState.JUGADOR_TURNO -> "¡Tu Turno!"
        GameState.GAME_OVER -> "¡Has Perdido!"
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
        Text(text = "Nivel: $level", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = statusText, fontSize = 20.sp, color = Color.LightGray)
    }
}

@Composable
fun SimonButtonsGrid(
    colors: List<ColorJuego>,
    enabled: Boolean,
    feedbackColor: Int?,
    onButtonClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f), // Cuadrado
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
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
    val isIlluminated = feedbackColor == colorJuego.id

    // Usar colorResource para obtener el color desde R.color
    val baseColor = colorResource(id = colorJuego.colorRes)

    val colorToUse = if (isIlluminated) baseColor.copy(alpha = 1f) else baseColor.copy(alpha = 0.5f)

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .padding(8.dp)
            .background(colorToUse, RoundedCornerShape(16.dp))
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
        GameState.GAME_OVER -> "REINICIAR JUEGO"
        else -> "JUGANDO"
    }

    Button(
        onClick = onStartClick,
        enabled = gameState == GameState.INICIO || gameState == GameState.GAME_OVER,
        modifier = Modifier.padding(16.dp)
    ) {
        Text(text = buttonText, fontSize = 20.sp)
    }
}