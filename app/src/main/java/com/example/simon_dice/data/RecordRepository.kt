package com.example.simon_dice.data

import com.example.simon_dice.data.local.RecordDao // Sigue siendo necesario para la implementación real

/**
 * [RecordRepository] es el punto de acceso central para los datos del récord.
 *
 * La arquitectura MVVM requiere un Repositorio para abstraer la fuente
 * de datos.
 *
 * **IMPORTANTE:** Ahora el constructor acepta una interfaz para permitir
 * el Mocking en pruebas unitarias sin dependencia de Android.
 *
 * @property recordDataSource La fuente de datos del récord (puede ser RecordDao o un Mock).
 */
open class RecordRepository(val recordDataSource: RecordDaoInterface) {

    /**
     * Interfaz que define las operaciones de datos necesarias.
     * Esta interfaz permite desacoplar el Repositorio de la implementación específica de Android (RecordDao).
     */
    interface RecordDaoInterface {
        fun loadRecord(): Record
        fun saveRecord(score: Int, timestamp: Long)
    }

    /**
     * Carga el récord actual desde la fuente de datos.
     */
    open fun getRecord(): Record {
        return recordDataSource.loadRecord()
    }

    /**
     * Comprueba si el score final es un nuevo récord y, si lo es, lo guarda.
     * @param finalLevel El nivel alcanzado.
     * @param currentRecord El récord actual.
     * @return True si se ha guardado un nuevo récord, False en caso contrario.
     */
    open fun updateRecordIfHigher(finalLevel: Int, currentRecord: Record): Boolean {
        // La puntuación real es el nivel final - 1 (porque el nivel se incrementa al inicio de la ronda)
        val finalScore = finalLevel - 1

        if (currentRecord.isNewRecord(finalScore)) {
            recordDataSource.saveRecord(finalScore, System.currentTimeMillis())
            return true
        }
        return false
    }
}