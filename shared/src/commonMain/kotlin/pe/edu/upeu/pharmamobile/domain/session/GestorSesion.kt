package pe.edu.upeu.pharmamobile.domain.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import pe.edu.upeu.pharmamobile.domain.model.Sesion
import pe.edu.upeu.pharmamobile.domain.repository.AuthRepository

const val AVISO_SESION_EXPIRADA = "Tu sesión expiró. Inicia sesión nuevamente."

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

    private val _aviso = MutableStateFlow<String?>(null)
    val aviso: StateFlow<String?> = _aviso.asStateFlow()

    /** Al arrancar: si hay token en el almacén, recupera la sesión sin pedir credenciales. */
    suspend fun restaurar() {
        _sesion.value = auth.sesionGuardada()
        _restaurando.value = false
    }

    fun iniciar(sesion: Sesion) {
        _aviso.value = null
        _sesion.value = sesion
    }

    /** Cierre voluntario: borra el token y vuelve al login sin aviso. */
    suspend fun cerrar() {
        auth.cerrarSesion()
        _sesion.value = null
    }

    /** 401 con sesión activa: borra el token, limpia el estado y deja un aviso para el login. */
    suspend fun expirar() {
        auth.cerrarSesion()
        _sesion.value = null
        _aviso.value = AVISO_SESION_EXPIRADA
    }

    fun consumirAviso() {
        _aviso.value = null
    }
}
