package com.example.simon_dice.data

/**
 * [Record] es el modelo de datos que representa el récord más alto del juego.
 *
 * @property score El número entero que representa la ronda más alta alcanzada
 * @property timestamp La marca de tiempo (dia y hora) en que se consiguió el récord,
 * representada como un Long
 *
 */
data class Record(
    val score: Int = 0,
    val timestamp: Long = 0L
) {
    /**
     * Comprueba si el score actual es mayor que el récord almacenado.
     * @param currentScore El score a comparar.
     * @return True si el score actual es un nuevo récord.
     */
    fun isNewRecord(currentScore: Int): Boolean {
        // Justificación: Un score de 0 se considera el valor inicial,
        // por lo que cualquier score > 0 es un nuevo récord si el anterior es 0.
        return currentScore > this.score
    }
}