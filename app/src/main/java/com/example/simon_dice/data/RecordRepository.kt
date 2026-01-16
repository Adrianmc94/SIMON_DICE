package com.example.simon_dice.data

import com.example.simon_dice.data.local.SharedPrefsManager
import com.example.simon_dice.data.remote.MongoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

open class RecordRepository(
    private val dao: RecordDaoInterface,
    private val prefs: SharedPrefsManager,
    private val mongo: MongoManager
) {
    interface RecordDaoInterface {
        fun loadRecord(): UserRecord?
        fun saveRecord(score: Int, timestamp: Long)
    }

    fun getRecord(): UserRecord? = dao.loadRecord()

    suspend fun updateRecordIfHigher(level: Int, current: UserRecord?): Boolean {
        val score = level - 1
        if (current == null || score > current.score) {
            val ts = System.currentTimeMillis()
            dao.saveRecord(score, ts)
            prefs.saveHighScore(score)
            withContext(Dispatchers.IO) { mongo.saveToCloud(score, ts) }
            return true
        }
        return false
    }
}