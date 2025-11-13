package com.example.simon_dice

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random


class SimonViewModel : ViewModel() {

    // live data
    private val _estadoJuego = MutableLiveData("Pulsa INICIO / REINICIAR")
    val estadoJuego: LiveData<String> = _estadoJuego

    private val _nivel = MutableLiveData(0)
    val nivel: LiveData<Int> = _nivel

    // Notifica a la activity qué color debe parpadear. Se limpia inmediatamente después de usarse.
    private val _colorAParpadear = MutableLiveData<Int?>(null)
    val colorAParpadear: LiveData<Int?> = _colorAParpadear

    // Indica a la activity que reproduzca el sonido de error
    private val _debeSonarError = MutableLiveData(false)
    val debeSonarError: LiveData<Boolean> = _debeSonarError

    private val _botonesHabilitados = MutableLiveData(false)
    val botonesHabilitados: LiveData<Boolean> = _botonesHabilitados

    private val _botonInicioHabilitado = MutableLiveData(true)
    val botonInicioHabilitado: LiveData<Boolean> = _botonInicioHabilitado


    // lógica interna
    private val secuencia = mutableListOf<Int>()
    private var indiceSecuenciaJugador = 0
    private var esTurnoSimon = false

    fun iniciarJuego() {
        if (!esTurnoSimon) {
            secuencia.clear()
            _nivel.value = 0
            indiceSecuenciaJugador = 0
            _botonInicioHabilitado.value = false
            turnoSimon()
        }
    }

    private fun turnoSimon() = viewModelScope.launch {
        esTurnoSimon = true
        _botonesHabilitados.value = false // Deshabilitar entrada del jugador

        _nivel.value = (_nivel.value ?: 0) + 1
        _estadoJuego.value = "Simón Muestra (Nivel ${_nivel.value})"

        // Añadir nuevo color a la secuencia
        val nuevoColor = Random.nextInt(0, 4)
        secuencia.add(nuevoColor)
        indiceSecuenciaJugador = 0

        delay(500L)

        // Muestra la secuencia completa
        for (color in secuencia) {
            _colorAParpadear.value = color
            _colorAParpadear.value = null // Limpiar evento
            delay(750L) // Pausa entre tonos
        }

        turnoJugador()
    }

    private fun turnoJugador() {
        esTurnoSimon = false
        _estadoJuego.value = "Tu Turno"
        _botonesHabilitados.value = true // Habilitar botones
    }

    fun manejarInputJugador(colorInput: Int) {
        if (esTurnoSimon || _botonesHabilitados.value == false) return // Ignorar input

        val colorEsperado = secuencia[indiceSecuenciaJugador]

        // Enviar feedback de parpadeo y sonido
        _colorAParpadear.value = colorInput
        _colorAParpadear.value = null

        if (colorInput == colorEsperado) {
            indiceSecuenciaJugador++

            if (indiceSecuenciaJugador == secuencia.size) { // Secuencia acertada
                turnoSimon()
            }
        } else {
            finalizarJuego()
        }
    }

    private fun finalizarJuego() {
        _debeSonarError.value = true
        _debeSonarError.value = false // Limpiar evento

        _estadoJuego.value = "¡Has Perdido! Nivel Alcanzado: ${_nivel.value}"
        _botonesHabilitados.value = false
        _botonInicioHabilitado.value = true
    }
}