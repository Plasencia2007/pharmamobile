package pe.edu.upeu.pharmamobile.domain.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pe.edu.upeu.pharmamobile.domain.model.Sesion
import pe.edu.upeu.pharmamobile.domain.repository.AuthRepository

const val AVISO_SESION_EXPIRADA = "Tu sesión expiró o ya no es válida. Inicia sesión nuevamente."

/**
 * Única fuente de verdad de "¿hay sesión?". La interfaz solo observa
 * [sesion]: nunca ve el token. Un 401 en cualquier llamada protegida
 * termina aquí (expirar); un 403 no.
 */
class GestorSesion(private val auth: AuthRepository) {

    private val _sesion = MutableStateFlow<Sesion?>(null)
    val sesion: StateFlow<Sesion?> = _sesion.asStateFlow()

    private val _restaurando = MutableStateFlow(true)
    val restaurando: StateFlow<Boolean> = _restaurando.asStateFlow()

    /** Cambia con cada inicio de sesión: las pantallas lo usan para no reutilizar datos de la sesión anterior. */
    private val _generacion = MutableStateFlow(0)
    val generacion: StateFlow<Int> = _generacion.asStateFlow()

    private val _aviso = MutableStateFlow<String?>(null)
    val aviso: StateFlow<String?> = _aviso.asStateFlow()

    /** Al arrancar: si hay token en el almacén, recupera la sesión sin pedir credenciales. */
    suspend fun restaurar() {
        _sesion.value = auth.sesionGuardada()
        _restaurando.value = false
    }

    fun iniciar(sesion: Sesion) {
        _aviso.value = null
        _generacion.value += 1
        _sesion.value = sesion
    }

    /** Cierre voluntario: el token ya fue borrado por CerrarSesionUseCase; vuelve al login sin aviso. */
    fun alCerrarSesion() {
        _sesion.value = null
    }

    /** 401 con sesión activa: borra el token, limpia el estado y deja un aviso para el login. */
    suspend fun expirar() {
        // Sin sesión y sin restauración en curso no hay nada que expirar: no se deja un aviso falso en el login.
        if (_sesion.value == null && !_restaurando.value) return
        auth.cerrarSesion()
        _sesion.value = null
        _aviso.value = AVISO_SESION_EXPIRADA
    }

    fun consumirAviso() {
        _aviso.value = null
    }
}
