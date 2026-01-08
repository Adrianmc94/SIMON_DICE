package com.example.simon_dice.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "record_table")
data class Record(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val score: Int,
    val timestamp: Long
) {
    /**
     * Lógica para comprobar si una nueva puntuación supera a la actual.
     */
    fun isNewRecord(finalScore: Int): Boolean = finalScore > score
}