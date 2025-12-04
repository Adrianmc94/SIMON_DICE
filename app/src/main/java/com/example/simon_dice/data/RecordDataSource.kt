package com.example.simon_dice.data

import com.example.simon_dice.model.Record

/**
 * Interfaz para la fuente de datos del récord.
 *
 * Referencia: [Guía para arquitecturas de apps | Android Developers](https://developer.android.com/topic/architecture/data-layer)
 * Este contrato permite cambiar la tecnología de persistencia (SharedPreferences a Room)
 * sin modificar el Repository o el ViewModel.
 */
interface RecordDataSource {
    /** Obtiene el récord actual almacenado. */
    fun getRecord(): Record

    /**
     * Guarda un nuevo récord si el [nuevoNivel] es mayor que el récord actual.
     * @return true si se guardó un nuevo récord.
     */
    fun saveNewRecord(nuevoNivel: Int): Boolean
}