package com.example.simon_dice.model
/**
 * Modelo de datos para almacenar el récord de la partida.
 *
 * @property nivel: La ronda más alta alcanzada por el jugador.
 * @property marcaTiempo: El día y la hora en que se consiguió el récord (usando un timestamp o String).
 */
data class Record(
    val nivel: Int = 0,
    val marcaTiempo: String = "" // Usaremos un String formateado para simplicidad
)