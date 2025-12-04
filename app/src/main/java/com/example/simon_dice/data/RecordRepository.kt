package com.example.simon_dice.data

import com.example.simon_dice.model.Record

/**
 * Repositorio de la capa de datos para gestionar la lógica de negocio del récord.
 *
 * Referencia: [Guía para arquitecturas de apps | Android Developers](https://developer.android.com/topic/architecture/data-layer)
 * Actúa como mediador, permitiendo al ViewModel acceder a los datos sin saber su origen.
 *
 * @param dataSource La fuente de datos inyectada (SharedPreferences o BD).
 */
class RecordRepository(private val dataSource: RecordDataSource) {

    /** Obtiene el récord actual. */
    fun getRecord(): Record = dataSource.getRecord()

    /** Intenta guardar un nuevo récord. */
    fun saveIfNewRecord(nivel: Int): Boolean = dataSource.saveNewRecord(nivel)
}