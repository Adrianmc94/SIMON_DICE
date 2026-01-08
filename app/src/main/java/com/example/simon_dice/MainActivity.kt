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
import android.text.format.DateFormat
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import com.example.simon_dice.data.RecordRepository
import com.example.simon_dice.data.local.AppDatabase
import java.util.Date

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
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = RecordRepository(database.recordDao())
        val viewModel: SimonViewModel = ViewModelProvider(this, SimonViewModelFactory(repository))[SimonViewModel::class.java]

        setContent {
            // Fondo Negro para toda la app
            Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121212)) {
                SimonDiceGame(viewModel)
            }
        }
    }
}

@Composable
fun SimonDiceGame(viewModel: SimonViewModel) {
    val gameState by viewModel.gameState.collectAsState()
    val level by viewModel.level.collectAsState()
    val highRecord by viewModel.highRecord.collectAsState()
    val feedbackColor by viewModel.feedbackColor.collectAsState()
    val tonoASonar by viewModel.tonoASonar.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(tonoASonar) {
        tonoASonar?.let { sonidoRes ->
            val mp = MediaPlayer.create(context, sonidoRes)
            mp.setOnCompletionListener { it.release(); viewModel.sonidoReproducido() }
            mp.start()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Texto en blanco para resaltar sobre el negro
        Text("SIMÓN DICE", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)

        Text("RÉCORD: ${highRecord.score}", fontSize = 18.sp, color = Color.LightGray)
        Text("NIVEL ACTUAL: $level", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colorResource(R.color.purple_200))

        Spacer(modifier = Modifier.height(20.dp))

        // TABLERO DE BOTONES (El peso 1f hace que ocupe el espacio sobrante)
        Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                ColorButton(ColorJuego.VERDE, feedbackColor == 0, gameState == GameState.JUGADOR_TURNO, Modifier.weight(1f)) { viewModel.manejarClickJugador(0) }
                ColorButton(ColorJuego.ROJO, feedbackColor == 1, gameState == GameState.JUGADOR_TURNO, Modifier.weight(1f)) { viewModel.manejarClickJugador(1) }
            }
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                ColorButton(ColorJuego.AZUL, feedbackColor == 2, gameState == GameState.JUGADOR_TURNO, Modifier.weight(1f)) { viewModel.manejarClickJugador(2) }
                ColorButton(ColorJuego.AMARILLO, feedbackColor == 3, gameState == GameState.JUGADOR_TURNO, Modifier.weight(1f)) { viewModel.manejarClickJugador(3) }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Botón de acción inferior
        if (gameState == GameState.INICIO || gameState == GameState.GAME_OVER) {
            Button(
                onClick = { viewModel.iniciarJuego() },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
            ) {
                Text(if (gameState == GameState.INICIO) "JUGAR" else "REINTENTAR", fontWeight = FontWeight.Bold)
            }
        } else {
            Text(
                if (gameState == GameState.SIMON_TURNO) "¡MIRA!" else "¡TU TURNO!",
                color = Color.Yellow, fontSize = 24.sp, fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ColorButton(colorJuego: ColorJuego, isIlluminated: Boolean, enabled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val baseColor = colorResource(id = colorJuego.colorRes)
    val finalColor = if (isIlluminated) baseColor else baseColor.copy(alpha = 0.3f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
            .background(finalColor, RoundedCornerShape(16.dp))
            .clickable(enabled = enabled) { onClick() }
    )
}