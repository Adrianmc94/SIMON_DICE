package com.example.simon_dice

// Importamos librerías para pruebas de corrutinas y flujos
import app.cash.turbine.testIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import app.cash.turbine.awaitItem
import kotlin.time.Duration.Companion.milliseconds

/**
 * Clase de pruebas unitarias para SimonViewModel.
 * Aquí comprobamos que el juego funciona como esperamos.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SimonViewModelTest {

    private lateinit var viewModel: SimonViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    // Constantes que coinciden con las del ViewModel
    private val FLASH_DURATION = 500L
    private val SEQUENCE_DELAY = 100L
    private val INITIAL_DELAY = 1000L
    private val R_DURATION = FLASH_DURATION + SEQUENCE_DELAY

    @Before
    fun setup() {
        // Ponemos un dispatcher de pruebas para que las corrutinas se comporten bien
        Dispatchers.setMain(testDispatcher)
        viewModel = SimonViewModel()
    }

    @After
    fun tearDown() {
        // Limpiamos después de cada test
        Dispatchers.resetMain()
    }

    // --- Test 1: comprobar que iniciar el juego cambia correctamente los estados ---
    @Test
    fun iniciarJuego_transicionaCorrectamente_aMostrarSecuencia_y_a_PlayerTurn() = runTest {
        val stateCollector = viewModel.uiState.testIn(backgroundScope)

        // Estado inicial
        assertEquals(GameState.Ready, stateCollector.awaitItem().state)

        viewModel.iniciarJuego()

        // Después de iniciar, debería crear secuencia y aumentar nivel
        val startState = stateCollector.awaitItem()
        assertEquals(GameState.Ready, startState.state)
        assertTrue(startState.sequence.isNotEmpty())

        // Ahora Simón muestra la secuencia
        assertEquals(GameState.SimonShowing, stateCollector.awaitItem().state)

        // Avanzamos el tiempo para que termine el flash de Simón
        advanceTimeBy((INITIAL_DELAY + R_DURATION).milliseconds)

        // Estado final: turno del jugador y botones habilitados
        assertEquals(GameState.PlayerTurn, stateCollector.awaitItem().state)
        assertEquals(true, stateCollector.awaitItem().isColorButtonsEnabled)

        stateCollector.cancelAndConsumeRemainingEvents()
    }

    // --- Test 2: comprobar que Simón emite los eventos de flash correctamente ---
    @Test
    fun secuenciaSimon_emiteEventosFlashColor_conTemporizacionCorrecta() = runTest {
        val eventCollector = viewModel.eventFlow.testIn(backgroundScope)

        viewModel.iniciarJuego()
        advanceTimeBy(INITIAL_DELAY.milliseconds)

        val flashEvent = eventCollector.awaitItem() as UiEvent.FlashColor
        val initialSequence = viewModel.uiState.value.sequence
        assertEquals(initialSequence.first().id, flashEvent.colorId)
        assertEquals(FLASH_DURATION, flashEvent.duration)

        advanceTimeBy(R_DURATION.milliseconds)
        eventCollector.cancelAndConsumeRemainingEvents()
    }

    // --- Test 3: comprobar que si el jugador acierta, el juego sigue al siguiente nivel (secuencia de 1) ---
    @Test
    fun inputJugador_secuenciaCorrecta_pasaAlSiguienteNivel() = runTest {
        val stateCollector = viewModel.uiState.testIn(backgroundScope)
        val eventCollector = viewModel.eventFlow.testIn(backgroundScope)

        stateCollector.awaitItem() // Estado inicial
        viewModel.iniciarJuego()
        stateCollector.skipItems(2) // Ready y SimonShowing

        advanceTimeBy((INITIAL_DELAY + R_DURATION).milliseconds)
        stateCollector.skipItems(2) // PlayerTurn y botones habilitados

        // Jugador pulsa el color correcto
        val correctColorId = viewModel.uiState.value.sequence.first().id
        viewModel.manejarInputJugador(correctColorId)

        // Debe emitirse el flash del input
        val flashEvent = eventCollector.awaitItem() as UiEvent.FlashColor
        assertEquals(correctColorId, flashEvent.colorId)

        // Estado: inputs limpios y nivel sube
        stateCollector.awaitItem() // inputs limpiados
        val nextStateReady = stateCollector.awaitItem()
        assertEquals(GameState.Ready, nextStateReady.state)
        assertEquals(2, nextStateReady.score)
        stateCollector.awaitItem() // SimonShowing siguiente ronda

        stateCollector.cancelAndConsumeRemainingEvents()
        eventCollector.cancelAndConsumeRemainingEvents()
    }

    // --- Test 4: comprobar que si el jugador falla, el juego termina ---
    @Test
    fun inputJugador_secuenciaIncorrecta_terminaElJuego() = runTest {
        val stateCollector = viewModel.uiState.testIn(backgroundScope)
        val eventCollector = viewModel.eventFlow.testIn(backgroundScope)

        stateCollector.awaitItem() // Inicial Ready
        viewModel.iniciarJuego()
        stateCollector.skipItems(2) // Ready y SimonShowing

        advanceTimeBy((INITIAL_DELAY + R_DURATION).milliseconds)
        stateCollector.skipItems(2) // PlayerTurn y botones habilitados

        // Jugador pulsa un color incorrecto
        val correctColorId = viewModel.uiState.value.sequence.first().id
        val incorrectColorId = (correctColorId + 1) % 4
        viewModel.manejarInputJugador(incorrectColorId)

        // Debe emitir flash del input y sonido de error
        val flashEvent = eventCollector.awaitItem() as UiEvent.FlashColor
        assertEquals(incorrectColorId, flashEvent.colorId)
        assertEquals(UiEvent.PlayErrorSound, eventCollector.awaitItem())

        // Estado: Game Over y botones actualizados
        stateCollector.awaitItem()
        val gameOverState = stateCollector.awaitItem()
        assertEquals(GameState.GameOver, gameOverState.state)
        assertEquals(false, gameOverState.isColorButtonsEnabled)
        assertEquals(true, gameOverState.isStartButtonEnabled)

        stateCollector.cancelAndConsumeRemainingEvents()
        eventCollector.cancelAndConsumeRemainingEvents()
    }

    // --- Test 5: comprobar que el jugador acierta una secuencia de múltiples pasos ---
    @Test
    fun inputJugador_secuenciaMultiplesPasosCorrecta_pasaAlSiguienteNivel() = runTest {
        // Necesitamos que la secuencia de Simón tenga 2 colores.

        val stateCollector = viewModel.uiState.testIn(backgroundScope)
        stateCollector.awaitItem() // 1. Ready inicial

        // === Ronda 1: Secuencia de 1 color ===
        viewModel.iniciarJuego()
        stateCollector.skipItems(2) // 2. Ready (score=1), 3. SimonShowing

        // Avanzamos el tiempo para que Simon muestre la secuencia (INITIAL_DELAY + R_DURATION)
        advanceTimeBy((INITIAL_DELAY + R_DURATION).milliseconds)
        stateCollector.skipItems(2) // 4. PlayerTurn, 5. isColorButtonsEnabled

        val colorRonda1 = viewModel.uiState.value.sequence.first().id
        viewModel.manejarInputJugador(colorRonda1) // Acierta 1er color -> Pasa a Ronda 2

        stateCollector.skipItems(3) // 6. playerInputs limpiados, 7. Ready (score=2), 8. SimonShowing (Ronda 2)

        // === Ronda 2: Secuencia de 2 colores ===

        // Avanzamos el tiempo de la secuencia de Simon (INITIAL_DELAY + R_DURATION * 2)
        advanceTimeBy(INITIAL_DELAY.milliseconds) // Espera inicial de Simon
        advanceTimeBy(R_DURATION.milliseconds) // Primer color
        advanceTimeBy(R_DURATION.milliseconds) // Segundo color

        stateCollector.skipItems(2) // 9. PlayerTurn (Ronda 2), 10. isColorButtonsEnabled (Habilitado)

        val secuenciaRonda2 = viewModel.uiState.value.sequence // Secuencia completa (2 colores)
        assertEquals(2, secuenciaRonda2.size)

        // 1. Jugador pulsa el primer color
        viewModel.manejarInputJugador(secuenciaRonda2[0].id)
        stateCollector.awaitItem() // Estado: playerInputs = [color1]
        assertEquals(1, viewModel.uiState.value.playerInputs.size)

        // 2. Jugador pulsa el segundo color (finaliza la ronda)
        viewModel.manejarInputJugador(secuenciaRonda2[1].id)

        // 11. playerInputs limpiados (antes de llamar a turnoSimon)
        val inputsCleaned = stateCollector.awaitItem()
        assertEquals(0, inputsCleaned.playerInputs.size)

        // 12. Ready (Nivel 3)
        val readyState = stateCollector.awaitItem()
        assertEquals(GameState.Ready, readyState.state)
        assertEquals(3, readyState.score) // Nivel 3

        // 13. SimonShowing (Ronda 3)
        stateCollector.awaitItem()

        stateCollector.cancelAndConsumeRemainingEvents()
    }
}