package com.example.simon_dice.data.local

import android.content.Context

class SharedPrefsManager(context: Context) {
    private val prefs = context.getSharedPreferences("simon_prefs", Context.MODE_PRIVATE)

    fun saveHighScore(score: Int) {
        prefs.edit().putInt("high_score", score).apply()
    }

    fun getHighScore(): Int = prefs.getInt("high_score", 0)
}