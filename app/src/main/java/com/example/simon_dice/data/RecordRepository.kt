package com.example.simon_dice.data

import com.example.simon_dice.data.local.RecordDao

open class RecordRepository(private val recordDataSource: RecordDaoInterface) {

    interface RecordDaoInterface {
        fun loadRecord(): UserRecord?
        fun saveRecord(score: Int, timestamp: Long)
    }

    open fun getRecord(): UserRecord? {
        return recordDataSource.loadRecord()
    }

    open fun updateRecordIfHigher(finalLevel: Int, currentRecord: UserRecord?): Boolean {
        val finalScore = finalLevel - 1
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