package com.example.simon_dice.data

import com.example.simon_dice.data.local.RecordDao

/**
 * [RecordRepository] es el punto de acceso central para los datos del récord.
 *
 * La arquitectura MVVM requiere un Repositorio para abstraer la fuente
 * de datos (actualmente Shared Preferences, pero mañana podría ser otra),
 * cumpliendo con el requisito de modularidad.
 *
 * @property recordDao El objeto de acceso a datos local.
 */
class RecordRepository(private val recordDao: RecordDao) {

    /**
     * Carga el récord actual desde la fuente de datos.
     */
    fun getRecord(): Record {
        return recordDao.loadRecord()
    }

    /**
     * Comprueba si el score final es un nuevo récord y, si lo es, lo guarda.
     * @param finalScore La puntuación obtenida al finalizar el juego.
     * @param currentRecord El récord que se comparará con la puntuación final.
     * @return True si se ha guardado un nuevo récord, False en caso contrario.
     */
    fun updateRecordIfHigher(finalScore: Int, currentRecord: Record): Boolean {
        if (currentRecord.isNewRecord(finalScore)) {
            // Se restaura 1 porque el nivel se incrementa al principio de la ronda
            // y el juego termina al fallo.
            val finalScoreToSave = finalScore - 1
            recordDao.saveRecord(finalScoreToSave, System.currentTimeMillis())
            return true
        }
        return false
    }
}