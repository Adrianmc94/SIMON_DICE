package com.example.simon_dice.model

/**
 * Modelo de datos que representa el récord más alto alcanzado.
 *
 * Referencia: [Kotlin Data Classes | Kotlin Docs](https://kotlinlang.org/docs/data-classes.html)
 * Se utiliza una 'data class' para la gestión de datos inmutables del récord.
 *
 * @property nivel: La ronda más alta alcanzada por el jugador (entero).
 * @property marcaTiempo: El día y la hora en formato String en que se consiguió el récord.
 */
data class Record(
    val nivel: Int = 0,
    val marcaTiempo: String = ""
)