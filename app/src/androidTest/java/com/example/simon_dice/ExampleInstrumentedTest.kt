package com.example.simon_dice

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.simon_dice.data.impl.RecordSharedPreferencesDataSource
import com.example.simon_dice.model.Record

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before

/**
 * Tests Instrumentados para [RecordSharedPreferencesDataSource].
 * Estos tests se ejecutan en un dispositivo o emulador Android, ya que requieren un [Context]
 * para interactuar con SharedPreferences.
 *
 * Referencia: [Instrumented testing | Android Developers](https://developer.android.com/training/testing/instrumented-testing)
 */
@RunWith(AndroidJUnit4::class)
class RecordDataSourceInstrumentedTest {

    private lateinit var dataSource: RecordSharedPreferencesDataSource
    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setup() {
        // Inicializamos el DataSource. Usamos un nombre de archivo de prueba para aislar el test.
        // Referencia: [Context.getSharedPreferences() | Android Developers]
        dataSource = RecordSharedPreferencesDataSource(appContext)

        // Limpiamos los datos de prueba antes de cada test.
        appContext.getSharedPreferences("SimonDiceRecords", android.content.Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    /**
     * Prueba que se puede guardar un nuevo récord y que se puede recuperar correctamente.
     * También verifica que la marca de tiempo no está vacía.
     */
    @Test
    fun saveNewRecord_and_getRecord_Success() {
        val newLevel = 5

        // Intentamos guardar el récord.
        val saved = dataSource.saveNewRecord(newLevel)
        assertTrue("Debe guardarse el récord la primera vez.", saved)

        // Recuperamos el récord.
        val retrievedRecord = dataSource.getRecord()

        // Verificamos los valores
        assertEquals("El nivel guardado debe ser 5", newLevel, retrievedRecord.nivel)
        assertTrue("La marca de tiempo no debe estar vacía", retrievedRecord.marcaTiempo.isNotEmpty())
    }

    /**
     * Prueba que un nivel más bajo no reemplaza el récord actual.
     */
    @Test
    fun saveNewRecord_lowerLevel_NotSaved() {
        // 1. Guardamos un récord inicial alto
        dataSource.saveNewRecord(10)
        val initialRecord = dataSource.getRecord()

        // 2. Intentamos guardar un nivel más bajo
        val saved = dataSource.saveNewRecord(5)

        // 3. Verificamos que no se guardó y que el récord se mantiene
        assertFalse("No debe guardarse un nivel menor.", saved)
        assertEquals("El nivel debe seguir siendo 10", 10, dataSource.getRecord().nivel)
        assertEquals("La marca de tiempo debe ser la inicial", initialRecord.marcaTiempo, dataSource.getRecord().marcaTiempo)
    }

    /**
     * Prueba que un nivel igual al récord actual no lo reemplaza.
     */
    @Test
    fun saveNewRecord_equalLevel_NotSaved() {
        // 1. Guardamos un récord inicial
        dataSource.saveNewRecord(7)
        val initialRecord = dataSource.getRecord()

        // 2. Intentamos guardar el mismo nivel
        val saved = dataSource.saveNewRecord(7)

        // 3. Verificamos que no se guardó y que el récord se mantiene
        assertFalse("No debe guardarse un nivel igual.", saved)
        assertEquals("El nivel debe seguir siendo 7", 7, dataSource.getRecord().nivel)
        assertEquals("La marca de tiempo debe ser la inicial", initialRecord.marcaTiempo, dataSource.getRecord().marcaTiempo)
    }
}