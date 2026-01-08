package com.example.simon_dice.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "record_table")
data class Record(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val score: Int,
    val timestamp: Long
) {
    fun isNewRecord(finalScore: Int): Boolean = finalScore > score
}