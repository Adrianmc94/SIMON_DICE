// RecordRepository.kt
package com.example.simon_dice.data

import com.example.simon_dice.data.local.SharedPrefsManager
import com.example.simon_dice.data.remote.MongoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecordRepository(
    private val recordDataSource: RecordDaoInterface,
    private val prefs: SharedPrefsManager,
    private val mongo: MongoManager
) {
    interface RecordDaoInterface {
        fun loadRecord(): UserRecord?
        fun saveRecord(score: Int, timestamp: Long)
    }

    fun getRecord(): UserRecord? = recordDataSource.loadRecord()

    suspend fun updateRecordIfHigher(finalLevel: Int, currentRecord: UserRecord?): Boolean {
        val finalScore = finalLevel - 1
        val isNew = if (currentRecord == null) finalScore > 0 else currentRecord.isNewRecord(finalScore)

        if (isNew) {
            val ts = System.currentTimeMillis()
            recordDataSource.saveRecord(finalScore, ts) // Room
            prefs.saveHighScore(finalScore)            // SharedPreferences
            withContext(Dispatchers.IO) {
                try { mongo.saveToCloud(finalScore, ts) } catch (e: Exception) { }
            }
            return true
        }
        return false
    }
}