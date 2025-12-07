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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simon_dice.R

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF202020)) {
                SimonDiceScreen()
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
    val simonSequence by viewModel.simonSequence.collectAsState()
    val context = LocalContext.current

    // Efecto secundario para la reproducción de tonos
    LaunchedEffect(feedbackColor) {
        feedbackColor?.let { colorId ->
            val toneResId = try {
                ColorJuego.fromId(colorId).tonoRes
            } catch (e: Exception) {
                println("Error al obtener recurso de tono: ${e.message}")
                return@let
            }

            val mediaPlayer = MediaPlayer.create(context, toneResId)
            mediaPlayer?.start()
            mediaPlayer?.setOnCompletionListener { it.release() }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        // Marcador y Mensaje de Estado
        ScoreAndStatus(gameState, level)

        // Botones de Simón
        SimonButtonsGrid(
            gameState = gameState,
            feedbackColor = feedbackColor,
            onButtonClick = viewModel::manejarClickJugador
        )

        // Botón de Inicio/Reinicio
        StartButton(gameState, viewModel::iniciarJuego)

        // Mostrar la secuencia generada en el LogCat (Requisito de simulación)
        LaunchedEffect(simonSequence) {
            if (simonSequence.isNotEmpty()) {
                // Mapear los IDs de color a nombres para el Log
                val colorNames = simonSequence.map { id ->
                    try {
                        ColorJuego.fromId(id).name
                    } catch (e: Exception) {
                        "UNKNOWN_COLOR"
                    }
                }
                println("SIMON SEQUENCE: $colorNames")
            }
        }
    }
}

// COMPONENTES REUTILIZABLES
@Composable
fun ScoreAndStatus(gameState: GameState, level: Int) {
    val statusText = when (gameState) {
        GameState.INICIO -> "Presiona START para Jugar"
        GameState.SIMON_TURNO -> "¡SIMÓN MUESTRA!"
        GameState.JUGADOR_TURNO -> "¡TU TURNO! Nivel $level"
        // Muestra el nivel que se quedó antes de perder
        GameState.GAME_OVER -> "¡JUEGO TERMINADO! Nivel Alcanzado: ${level - 1}"
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Nivel Actual: $level",
            fontSize = 24.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = statusText,
            fontSize = 18.sp,
            color = when (gameState) {
                GameState.GAME_OVER -> Color.Red
                GameState.JUGADOR_TURNO -> Color(0xFF64FFDA) // Verde-Aqua
                else -> Color.LightGray
            }
        )
    }
}

@Composable
fun SimonButtonsGrid(
    gameState: GameState,
    feedbackColor: Int?,
    onButtonClick: (Int) -> Unit
) {
    // Usamos el enum ColorJuego que está en el SimonViewModel
    val colors = ColorJuego.entries.toList()
    // Los botones solo están habilitados si estamos en el turno del jugador
    val enabled = gameState == GameState.JUGADOR_TURNO

    Column(
        modifier = Modifier
            .size(300.dp)
            .padding(8.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SimonButton(colors[0], enabled, feedbackColor, onButtonClick) // VERDE
            SimonButton(colors[1], enabled, feedbackColor, onButtonClick) // ROJO
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
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
    // Verificar si este botón debe estar iluminado
    val isIlluminated = feedbackColor == colorJuego.id

    val baseColor = colorResource(id = colorJuego.colorRes)

    val colorToUse = if (isIlluminated) baseColor.copy(alpha = 1f) else baseColor.copy(alpha = 0.5f)

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .padding(8.dp)
            .background(colorToUse, RoundedCornerShape(16.dp))
            // Deshabilita el clickable si no está enabled
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
    Button(
        onClick = onStartClick,
        enabled = gameState == GameState.INICIO || gameState == GameState.GAME_OVER,
        modifier = Modifier
            .fillMaxWidth(0.5f)
            .height(50.dp)
    ) {
        Text(
            text = if (gameState == GameState.GAME_OVER) "REINICIAR" else "START",
            fontSize = 20.sp
        )
    }
}