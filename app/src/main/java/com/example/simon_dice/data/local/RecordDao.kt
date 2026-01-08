package com.example.simon_dice.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.simon_dice.data.UserRecord
import com.example.simon_dice.data.RecordRepository

@Dao
interface RecordDao : RecordRepository.RecordDaoInterface {

    @Query("SELECT * FROM user_records ORDER BY score DESC LIMIT 1")
    override fun loadRecord(): UserRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecord(record: UserRecord)

    override fun saveRecord(score: Int, timestamp: Long) {
        val nuevoRecord = UserRecord(score = score, timestamp = timestamp)
        insertRecord(nuevoRecord)
    }
}