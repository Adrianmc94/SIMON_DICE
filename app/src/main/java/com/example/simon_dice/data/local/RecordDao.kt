package com.example.simon_dice.data.local

import android.content.Context
import com.example.simon_dice.data.Record
import com.example.simon_dice.data.RecordRepository

/**
 * [RecordDao] gestiona la persistencia de datos del récord localmente
 * utilizando Shared Preferences.
 * Usamos Shared Preferences para el requisito inicial de persistencia
 * simple.
 *
 * Implementa la interfaz para que pueda ser inyectado en RecordRepository
 * y permitir el mocking en tests unitarios.
 *
 * @property context El contexto de la aplicación para acceder a Shared Preferences.
 */
class RecordDao(private val context: Context) : RecordRepository.RecordDaoInterface {
    // Nombre del archivo de Shared Preferences
    private val PREFS_NAME = "SimonDicePrefs"
    private val KEY_SCORE = "high_score"
    private val KEY_TIMESTAMP = "timestamp"

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Carga el récord almacenado de Shared Preferences.
     * @return El objeto Record con la puntuación y marca de tiempo almacenadas.
     */
    override fun loadRecord(): Record {
        // Si no existe, devuelve los valores por defecto
        val score = prefs.getInt(KEY_SCORE, 0)
        val timestamp = prefs.getLong(KEY_TIMESTAMP, 0L)
        return Record(score, timestamp)
    }

    /**
     * Guarda un nuevo récord en Shared Preferences.
     * @param score La nueva puntuación más alta.
     * @param timestamp La marca de tiempo del nuevo récord.
     */
    override fun saveRecord(score: Int, timestamp: Long) {
        prefs.edit().apply {
            putInt(KEY_SCORE, score)
            putLong(KEY_TIMESTAMP, timestamp)
            apply() // Guardar asíncronamente
        }
    }
}