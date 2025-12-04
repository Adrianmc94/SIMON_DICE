package com.example.simon_dice.data.impl

import android.content.Context
import android.content.SharedPreferences
import com.example.simon_dice.data.RecordDataSource
import com.example.simon_dice.model.Record
import java.text.SimpleDateFormat
import java.util.*

/**
 * Implementación de [RecordDataSource] que utiliza SharedPreferences.
 *
 * Referencia: [SharedPreferences | Android Developers](https://developer.android.com/reference/android/content/SharedPreferences)
 * Se utiliza para la persistencia de datos simples clave-valor.
 *
 * @param context El contexto de la aplicación para inicializar SharedPreferences.
 */
class RecordSharedPreferencesDataSource(context: Context) : RecordDataSource {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("SimonDiceRecords", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_NIVEL = "record_nivel"
        private const val KEY_TIMESTAMP = "record_timestamp"
    }

    /** @see RecordDataSource.getRecord */
    override fun getRecord(): Record {
        val nivel = sharedPreferences.getInt(KEY_NIVEL, 0)
        val timestamp = sharedPreferences.getString(KEY_TIMESTAMP, "") ?: ""
        return Record(nivel, timestamp)
    }

    /** @see RecordDataSource.saveNewRecord */
    override fun saveNewRecord(nuevoNivel: Int): Boolean {
        val recordActual = getRecord()

        if (nuevoNivel > recordActual.nivel) {
            val editor = sharedPreferences.edit()

            // Formatea la marca de tiempo (día y hora)
            val now = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())

            editor.putInt(KEY_NIVEL, nuevoNivel)
            editor.putString(KEY_TIMESTAMP, now)

            // Referencia: [Editor.apply() vs Editor.commit() | Android Developers]
            // .apply() guarda de forma asíncrona.
            editor.apply()

            return true
        }
        return false
    }
}