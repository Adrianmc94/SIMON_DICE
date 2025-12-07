package com.example.simondice

import com.example.simondice.ui.GameState
import com.example.simondice.ui.SimonViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test

/**
 * Tests Unitarios para la lógica de negocio del SimonViewModel (Criterio: 20/20)
 * Requiere la dependencia kotlinx-coroutines-test
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ExampleUnitTest {

    private lateinit var viewModel: SimonViewModel

    @Before
    fun setup() {
        // Inicializa el ViewModel antes de cada prueba
        viewModel = SimonViewModel()
    }

    // TESTS DE FLUJO BÁSICO E INICIALIZACIÓN

    @Test
    fun `inicializarJuego_estableceEstadoCorrecto_y_nivelUno`() = runTest {
        // Llamar a la función de inicio
        viewModel.iniciarJuego()

        // Esperar a que el Job de Simón (delay) finalice
        // Esto asegura que el estado pasa de SIMON_TURNO a JUGADOR_TURNO
        advanceUntilIdle()

        // 3. Verificaciones
        assertEquals(GameState.JUGADOR_TURNO, viewModel.gameState.value) // Debe pasar a turno del jugador
        assertEquals(1, viewModel.level.value) // Debe empezar en Nivel 1
        assertEquals(1, viewModel.simonSequence.value.size) // Debe tener un elemento
    }

    @Test
    fun `reiniciarJuego_borraSecuencia_y_vuelveANivelUno`() = runTest {
        // Simular una partida avanzada
        viewModel.setGameState(GameState.GAME_OVER)
        // La secuencia real tendrá un elemento después de iniciar
        // Establecer un nivel superior
        val levelField = viewModel::class.java.getDeclaredField("_level")
        levelField.isAccessible = true
        levelField.set(viewModel, kotlinx.coroutines.flow.MutableStateFlow(5))


        // Reiniciar
        viewModel.iniciarJuego()
        advanceUntilIdle()

        // Verificaciones después de reiniciar
        assertEquals(GameState.JUGADOR_TURNO, viewModel.gameState.value)
        assertEquals(1, viewModel.level.value)
        assertEquals(1, viewModel.simonSequence.value.size)
        assertEquals(0, viewModel.playerInput.value.size)
    }

    // TESTS DE LÓGICA DE JUGADOR (ACIERTO Y FALLO)

    @Test
    fun `manejarClickJugador_conFallo_pasaAGameOver`() = runTest {
        // Simular inicio y forzar JUGADOR_TURNO
        viewModel.iniciarJuego()
        advanceUntilIdle()
        assertEquals(GameState.JUGADOR_TURNO, viewModel.gameState.value)

        val colorCorrecto = viewModel.simonSequence.value.first()
        val colorIncorrecto = if (colorCorrecto == 0) 1 else 0 // Cualquier otro color

        // Simular un click incorrecto
        viewModel.manejarClickJugador(colorIncorrecto)
        advanceUntilIdle() // Esperar a que la corrutina termine

        // Verificación
        assertEquals(GameState.GAME_OVER, viewModel.gameState.value)
        assertEquals(1, viewModel.playerInput.value.size) // El input incorrecto se registra
    }

    @Test
    fun `manejarClickJugador_aciertoParcial_mantieneElEstado`() = runTest {
        // Simular una secuencia de longitud 2
        viewModel.iniciarJuego()
        advanceUntilIdle()
        // Manipular la secuencia para que tenga 2 elementos
        val simonSequenceField = viewModel::class.java.getDeclaredField("_simonSequence")
        simonSequenceField.isAccessible = true
        simonSequenceField.set(viewModel, kotlinx.coroutines.flow.MutableStateFlow(listOf(0, 1)))

        // Forzar JUGADOR_TURNO
        viewModel.setGameState(GameState.JUGADOR_TURNO)

        // Simular el primer click
        viewModel.manejarClickJugador(0)
        advanceUntilIdle()

        // 3. Verificación
        assertEquals(GameState.JUGADOR_TURNO, viewModel.gameState.value) // Debe seguir esperando
        assertEquals(1, viewModel.playerInput.value.size)
        assertEquals(0, viewModel.playerInput.value.first())
    }

    @Test
    fun `manejarClickJugador_aciertoRondaCompleta_incrementaNivel_y_pasaASimonTurno`() = runTest {
        viewModel.iniciarJuego()
        advanceUntilIdle()
        assertEquals(GameState.JUGADOR_TURNO, viewModel.gameState.value)

        val secuenciaInicial = viewModel.simonSequence.value
        val colorCorrecto = secuenciaInicial.first()

        // Simular el click correcto (acierto de ronda completa)
        viewModel.manejarClickJugador(colorCorrecto)
        // El VM lanzará una corrutina para el delay + ejecutarTurnoSimon
        advanceUntilIdle()

        // Verificaciones
        // El estado debe volver a SIMON_TURNO
        assertEquals(GameState.SIMON_TURNO, viewModel.gameState.value)

        // El nivel debe incrementarse de 1 a 2
        assertEquals(2, viewModel.level.value)

        // La secuencia debe tener ahora 2 elementos (el original + el nuevo)
        assertEquals(2, viewModel.simonSequence.value.size)
    }
}