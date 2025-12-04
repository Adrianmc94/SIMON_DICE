package com.example.simon_dice.data

import com.example.simon_dice.model.Record

/**
 * Repositorio de la capa de datos para gestionar la lógica de negocio del récord.
 *
 * Proporciona una API limpia al ViewModel, desacoplando la capa UI/ViewModel
 * de la fuente de datos específica.
 *
 * Referencia: [Guía para arquitecturas de apps | Android Developers](https://developer.android.com/topic/architecture/data-layer)
 * Esta capa es responsable de decidir qué fuente de datos usar.
 *
 * @param dataSource La fuente de datos a utilizar (SharedPreferences, Room, etc.).
 */
class RecordRepository(private val dataSource: RecordDataSource) {

    /**
     * Obtiene el récord actual.
     */
    fun getRecord(): Record = dataSource.getRecord()

    /**
     * Intenta guardar un nuevo récord. La lógica de comparación está en el DataSource.
     *
     * @param nivel El nivel final alcanzado por el jugador.
     * @return true si se guardó un nuevo récord, false en caso contrario.
     */
    fun saveIfNewRecord(nivel: Int): Boolean = dataSource.saveNewRecord(nivel)
}