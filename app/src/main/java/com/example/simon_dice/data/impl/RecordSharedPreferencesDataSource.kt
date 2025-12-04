package com.example.simon_dice.data.impl

import android.content.Context
import android.content.SharedPreferences
import com.example.simon_dice.data.RecordDataSource
import com.example.simon_dice.model.Record
import java.text.SimpleDateFormat
import java.util.*

/**
 * Implementación de [RecordDataSource] que utiliza SharedPreferences para la persistencia.
 *
 * Referencia: [SharedPreferences | Android Developers](https://developer.android.com/reference/android/content/SharedPreferences)
 * para el almacenamiento persistente de datos simples clave-valor.
 *
 * @param context El contexto de la aplicación para acceder a SharedPreferences.
 */
class RecordSharedPreferencesDataSource(context: Context) : RecordDataSource {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("SimonDiceRecords", Context.MODE_PRIVATE)

    companion object {
        // Claves para SharedPreferences
        private const val KEY_NIVEL = "record_nivel"
        private const val KEY_TIMESTAMP = "record_timestamp"
    }

    /**
     * @see RecordDataSource.getRecord
     */
    override fun getRecord(): Record {
        // Obtenemos los valores. Si no existen, los valores por defecto son 0 y ""
        val nivel = sharedPreferences.getInt(KEY_NIVEL, 0)
        // Usamos el operador Elvis para asegurar que el resultado no es nulo, aunque el default es ""
        val timestamp = sharedPreferences.getString(KEY_TIMESTAMP, "") ?: ""
        return Record(nivel, timestamp)
    }

    /**
     * @see RecordDataSource.saveNewRecord
     */
    override fun saveNewRecord(nuevoNivel: Int): Boolean {
        val recordActual = getRecord()

        // Solo guardamos si el nuevo nivel supera el récord actual
        if (nuevoNivel > recordActual.nivel) {
            val editor = sharedPreferences.edit()

            // Formateamos la marca de tiempo actual
            val now = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())

            editor.putInt(KEY_NIVEL, nuevoNivel)
            editor.putString(KEY_TIMESTAMP, now)

            // Referencia: [Editor.apply() vs Editor.commit() | Android Developers]
            // .apply() es preferido para escrituras asíncronas que no necesitan un valor de retorno inmediato.
            editor.apply()

            return true
        }
        return false
    }
}