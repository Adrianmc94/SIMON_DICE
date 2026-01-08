package com.example.simon_dice.data

import com.example.simon_dice.data.local.RecordDao

/**
 * [RecordRepository] es el punto de acceso central para los datos del récord.
 * La arquitectura MVVM requiere un Repositorio para abstraer la fuente de datos.
 */
open class RecordRepository(val recordDataSource: RecordDaoInterface) {

    /**
     * Interfaz que define las operaciones de datos necesarias.
     * Esta interfaz permite desacoplar el Repositorio de la implementación específica de Android (RecordDao).
     */
    interface RecordDaoInterface {
        fun loadRecord(): Record?
        fun saveRecord(score: Int, timestamp: Long)
    }

    /**
     * Carga el récord actual desde la fuente de datos.
     * Se maneja la posibilidad de que no exista un récord previo (null).
     */
    open fun getRecord(): Record? {
        return recordDataSource.loadRecord()
    }

    /**
     * Comprueba si el score final es un nuevo récord y, si lo es, lo guarda.
     * @param finalLevel El nivel alcanzado.
     * @param currentRecord El récord actual (puede ser un Record vacío si es la primera vez).
     * @return True si se ha guardado un nuevo récord, False en caso contrario.
     */
    open fun updateRecordIfHigher(finalLevel: Int, currentRecord: Record?): Boolean {
        // La puntuación real es el nivel final - 1
        val finalScore = finalLevel - 1

        // Si no hay récord previo, cualquier puntuación > 0 es récord.
        // Si hay récord, usamos la función lógica de la propia entidad Record.
        val isNew = if (currentRecord == null) {
            finalScore > 0
        } else {
            currentRecord.isNewRecord(finalScore)
        }

        if (isNew) {
            recordDataSource.saveRecord(finalScore, System.currentTimeMillis())
            return true
        }
        return false
    }
}