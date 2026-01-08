package com.example.simon_dice.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_records")
data class UserRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val score: Int = 0,
    val timestamp: Long = 0L
) {
    fun isNewRecord(finalScore: Int): Boolean = finalScore > score
}