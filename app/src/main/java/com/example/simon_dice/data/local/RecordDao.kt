package com.example.simon_dice.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.simon_dice.data.Record
import com.example.simon_dice.data.RecordRepository

@Dao
interface RecordDao : RecordRepository.RecordDaoInterface {

    @Query("SELECT * FROM record_table ORDER BY score DESC LIMIT 1")
    override fun loadRecord(): Record?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecord(record: Record)

    // Implementación del método que requiere tu RecordRepository
    override fun saveRecord(score: Int, timestamp: Long) {
        val nuevoRecord = Record(score = score, timestamp = timestamp)
        insertRecord(nuevoRecord)
    }
}