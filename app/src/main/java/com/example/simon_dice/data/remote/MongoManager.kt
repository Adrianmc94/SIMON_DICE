package com.example.simon_dice.data.remote
import android.util.Log
import kotlinx.coroutines.delay

class MongoManager {
    suspend fun saveToCloud(score: Int, ts: Long) {
        delay(500)
        Log.d("MongoDB", "Guardado: $score puntos")
    }
}