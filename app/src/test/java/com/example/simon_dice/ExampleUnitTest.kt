package com.example.simon_dice

import com.example.simon_dice.data.RecordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExampleUnitTest {

    private lateinit var viewModel: SimonViewModel
    private lateinit var mockDataSource: MockRecordDataSource
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockDataSource = MockRecordDataSource()
        viewModel = SimonViewModel(RecordRepository(mockDataSource))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun iniciarJuego_debe_configurar_estado_inicial_correctamente() = testScope.runTest {
        viewModel.setLevel(5)
        viewModel.setSimonSequence(listOf(0, 1, 2))
        viewModel.setPlayerInput(listOf(0, 1))

        viewModel.iniciarJuego()

        assertEquals(1, viewModel.level.value)
        assertTrue(viewModel.simonSequence.value.isEmpty())
        assertTrue(viewModel.playerInput.value.isEmpty())
        assertEquals(GameState.SIMON_TURNO, viewModel.gameState.value)
    }

    @Test
    fun manejarClickJugador_debe_ignorar_clicks_cuando_no_es_turno_del_jugador() = testScope.runTest {
        viewModel.setGameState(GameState.SIMON_TURNO)
        viewModel.setSimonSequence(listOf(0))
        viewModel.manejarClickJugador(0)

        assertTrue(viewModel.playerInput.value.isEmpty())
    }

    @Test
    fun manejarClickJugador_debe_agregar_input_correcto_y_pasar_al_siguiente_nivel() = testScope.runTest {
        viewModel.setGameState(GameState.JUGADOR_TURNO)
        viewModel.setLevel(2)
        viewModel.setSimonSequence(listOf(0, 1))

        viewModel.manejarClickJugador(0)
        assertEquals(listOf(0), viewModel.playerInput.value)

        viewModel.manejarClickJugador(1)

        advanceTimeBy(500)
        assertEquals(GameState.SIMON_TURNO, viewModel.gameState.value)
        assertEquals(3, viewModel.level.value)
    }

    @Test
    fun manejarClickJugador_debe_detectar_error_y_cambiar_a_GAME_OVER() = testScope.runTest {
        viewModel.setGameState(GameState.JUGADOR_TURNO)
        viewModel.setSimonSequence(listOf(0))

        viewModel.manejarClickJugador(1)

        assertEquals(GameState.GAME_OVER, viewModel.gameState.value)
    }

    @Test
    fun manejarClickJugador_debe_guardar_record_cuando_se_supera_el_anterior() = testScope.runTest {
        mockDataSource.testRecord = Record(score = 3, timestamp = 1000L)
        viewModel.setGameState(GameState.JUGADOR_TURNO)
        viewModel.setLevel(5)
        viewModel.setSimonSequence(listOf(0, 1, 2, 3))

        viewModel.manejarClickJugador(3)

        advanceTimeBy(100)
        assertEquals(GameState.GAME_OVER, viewModel.gameState.value)
    }

    @Test
    fun Record_isNewRecord_debe_funcionar_correctamente() {
        val record = Record(score = 5, timestamp = 1000L)

        assertTrue(record.isNewRecord(6))
        assertFalse(record.isNewRecord(5))
        assertFalse(record.isNewRecord(4))
        assertTrue(Record().isNewRecord(1))
    }

    @Test
    fun agregarNuevoColor_debe_agregar_color_valido() {
        viewModel.setSimonSequence(listOf(0, 1))

        val method = SimonViewModel::class.java.getDeclaredMethod("agregarNuevoColor")
        method.isAccessible = true
        method.invoke(viewModel)

        assertEquals(3, viewModel.simonSequence.value.size)
        val nuevoColor = viewModel.simonSequence.value.last()
        assertTrue(nuevoColor in 0..3)
    }

    @Test
    fun saveHighRecordIfNew_debe_guardar_cuando_es_nuevo_record() = testScope.runTest {
        mockDataSource.testRecord = Record(score = 3, timestamp = 1000L)
        viewModel.setLevel(5)

        val method = SimonViewModel::class.java.getDeclaredMethod("saveHighRecordIfNew")
        method.isAccessible = true
        method.invoke(viewModel)

        advanceTimeBy(100)
        assertEquals(4, mockDataSource.savedScore) // 5 - 1 = 4
    }

    @Test
    fun saveHighRecordIfNew_no_debe_guardar_cuando_no_es_record() = testScope.runTest {
        mockDataSource.testRecord = Record(score = 10, timestamp = 1000L)
        viewModel.setLevel(5)

        val method = SimonViewModel::class.java.getDeclaredMethod("saveHighRecordIfNew")
        method.isAccessible = true
        method.invoke(viewModel)

        advanceTimeBy(100)
        assertFalse(mockDataSource.saveCalled)
    }
}

// Mock de la interfaz RecordDaoInterface
class MockRecordDataSource : RecordRepository.RecordDaoInterface {
    var testRecord = Record(score = 0, timestamp = 0L)
    var saveCalled = false
    var savedScore: Int = 0
    var savedTimestamp: Long = 0L

    override fun loadRecord(): Record = testRecord

    override fun saveRecord(score: Int, timestamp: Long) {
        saveCalled = true
        savedScore = score
        savedTimestamp = timestamp
        testRecord = Record(score, timestamp)
    }
}