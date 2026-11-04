package pe.edu.upeu.pharmamobile.presentation.sincronizacion

/** Aviso cuando la sincronizacion fallo por falta de red, no por un rechazo del servidor. */
const val AVISO_SIN_CONEXION = "Sin conexión: mostrando datos guardados"

/**
 * Lo que el usuario debe poder responder sin abrir ningun menu: si hay
 * conexion, cuantos cambios faltan por enviar y de cuando son los datos. Se
 * arma en el ViewModel con consultas observables de la base local, nunca
 * preguntandole a la red.
 */
data class SincronizacionUiState(
    val enCurso: Boolean = false,
    val pendientes: Int = 0,
    val fallidas: Int = 0,
    /** Epoch en milisegundos; null si este telefono nunca sincronizo. */
    val ultimaSync: Long? = null,
    val aviso: String? = null
) {
    val sinConexion: Boolean get() = aviso == AVISO_SIN_CONEXION
}

/** Cuantos cambios, con el verbo en el numero correcto: "1 cambio" / "3 cambios". */
fun cambios(cantidad: Int): String = if (cantidad == 1) "1 cambio" else "$cantidad cambios"
