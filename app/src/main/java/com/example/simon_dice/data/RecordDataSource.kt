package com.example.simon_dice.data
/**
 * Interfaz para la fuente de datos del récord.
 * Esto permite cambiar la implementación subyacente (SharedPreferences, Room, etc.)
 * sin modificar el Repository o el ViewModel.
 *
 * Referencia: Principios SOLID (Inversión de Dependencia).
 */
interface RecordDataSource {
    /**
     * Obtiene el récord actual almacenado.
     * @return El objeto [Record] con el nivel y la marca de tiempo.
     */
    fun getRecord(): Record

    /**
     * Guarda un nuevo récord si el nivel es mayor que el récord actual.
     *
     * @param nuevoNivel El nivel alcanzado.
     * @return true si se guardó un nuevo récord, false en caso contrario.
     */
    fun saveNewRecord(nuevoNivel: Int): Boolean
}