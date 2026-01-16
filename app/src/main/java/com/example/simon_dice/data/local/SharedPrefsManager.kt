package com.example.simon_dice.data.local
import android.content.Context

class SharedPrefsManager(context: Context) {
    private val p = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    fun saveHighScore(s: Int) = p.edit().putInt("high", s).apply()
}