package com.example.simon_dice.data.remote

import android.util.Log

class MongoManager {
    suspend fun saveToCloud(score: Int, ts: Long) {
        // En un entorno profesional aquí conectarías con MongoClient.create(URI)
        // Para el nivel de FP, simulamos la inserción asíncrona
        Log.d("MongoDB", "Guardando récord de $score en la nube...")
    }
}