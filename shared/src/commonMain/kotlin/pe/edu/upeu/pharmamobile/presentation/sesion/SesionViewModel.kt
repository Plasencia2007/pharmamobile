package pe.edu.upeu.pharmamobile.presentation.sesion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobile.data.remote.mensajeDeSesion
import pe.edu.upeu.pharmamobile.domain.model.ErrorSesion
import pe.edu.upeu.pharmamobile.domain.model.ErrorSesionException
import pe.edu.upeu.pharmamobile.domain.model.Sesion
import pe.edu.upeu.pharmamobile.domain.session.GestorSesion
import pe.edu.upeu.pharmamobile.domain.usecase.AlterarTokenGuardadoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.CerrarSesionUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.IniciarSesionUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.ProbarAccesoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.validarCredenciales

/** Estado de la pantalla de login y de la sesión. La interfaz nunca ve el token. */
data class SesionUiState(
    val usuario: String = "",
    val clave: String = "",
    val cargando: Boolean = false,
    val error: String? = null,
    val sesion: Sesion? = null,
    val errorUsuario: String? = null,
    val errorClave: String? = null,
    val aviso: String? = null,
    val restaurando: Boolean = true,
    val generacion: Int = 0,
    val diagnostico: Diagnostico = Diagnostico.Ninguno
) {
    sealed interface Diagnostico {
        data object Ninguno : Diagnostico
        data object Consultando : Diagnostico
        data class Informacion(val mensaje: String) : Diagnostico
        data class Permitido(val recurso: String) : Diagnostico
        data class Denegado(val mensaje: String) : Diagnostico
    }
}

class SesionViewModel(
    private val gestor: GestorSesion,
    private val iniciarSesionUseCase: IniciarSesionUseCase,
    private val cerrarSesionUseCase: CerrarSesionUseCase,
    private val probarAccesoUseCase: ProbarAccesoUseCase,
    private val alterarTokenUseCase: AlterarTokenGuardadoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SesionUiState())
    val uiState: StateFlow<SesionUiState> = _uiState.asStateFlow()

    init {
        // La UI refleja lo que dice el gestor: un 401 en cualquier pantalla
        // pone sesion en null y el login aparece solo, con su aviso.
        viewModelScope.launch {
            gestor.sesion.collect { s ->
                // Al perder la sesión (cierre o expiración) no queda el usuario anterior en el formulario.
                _uiState.update {
                    if (s == null) it.copy(sesion = null, usuario = "", clave = "", error = null,
                        errorUsuario = null, errorClave = null, diagnostico = SesionUiState.Diagnostico.Ninguno)
                    else it.copy(sesion = s)
                }
            }
        }
        viewModelScope.launch { gestor.generacion.collect { g -> _uiState.update { it.copy(generacion = g) } } }
        viewModelScope.launch { gestor.aviso.collect { a -> _uiState.update { it.copy(aviso = a) } } }
        viewModelScope.launch { gestor.restaurando.collect { r -> _uiState.update { it.copy(restaurando = r) } } }
        // Al arrancar: si hay un token guardado se recupera la sesión sin pedir credenciales.
        viewModelScope.launch { gestor.restaurar() }
    }

    fun alCambiarUsuario(valor: String) =
        _uiState.update { it.copy(usuario = valor, errorUsuario = null, error = null) }

    fun alCambiarClave(valor: String) =
        _uiState.update { it.copy(clave = valor, errorClave = null, error = null) }

    fun iniciarSesion() {
        val actual = _uiState.value
        if (actual.cargando) return
        val errores = validarCredenciales(actual.usuario, actual.clave)
        if (errores.hayErrores) {
            _uiState.update { it.copy(errorUsuario = errores.usuario, errorClave = errores.clave) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            iniciarSesionUseCase(actual.usuario, actual.clave)
                .onSuccess { sesion ->
                    gestor.iniciar(sesion)
                    _uiState.update {
                        it.copy(cargando = false, clave = "", diagnostico = SesionUiState.Diagnostico.Ninguno)
                    }
                }
                .onFailure { fallo ->
                    val motivo = (fallo as? ErrorSesionException)?.error ?: ErrorSesion.SinConexion
                    _uiState.update { it.copy(cargando = false, error = mensajeDeSesion(motivo)) }
                }
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            cerrarSesionUseCase()
            gestor.alCerrarSesion()
            _uiState.update { SesionUiState(restaurando = false, generacion = it.generacion) }
        }
    }

    fun consumirAviso() = gestor.consumirAviso()

    /** Solo depuración (escenario 3 de la actividad autónoma): cambia un carácter del token guardado. */
    fun alterarTokenGuardado() {
        viewModelScope.launch {
            val alterado = alterarTokenUseCase()
            _uiState.update {
                it.copy(diagnostico = SesionUiState.Diagnostico.Informacion(
                    if (alterado) "Token alterado. Pulsa «Probar acceso a clientes» para enviarlo."
                    else "No hay token guardado."))
            }
        }
    }

    /**
     * Diagnóstico de permisos: pide un recurso protegido y muestra lo que
     * responde el servidor. Un 403 no cierra la sesión; un 401 sí (lo
     * resuelve el cliente HTTP a través del gestor).
     */
    fun probarAcceso(recurso: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(diagnostico = SesionUiState.Diagnostico.Consultando) }
            probarAccesoUseCase(recurso)
                .onSuccess {
                    _uiState.update { it.copy(diagnostico = SesionUiState.Diagnostico.Permitido(recurso)) }
                }
                .onFailure { fallo ->
                    val motivo = (fallo as? ErrorSesionException)?.error ?: ErrorSesion.SinConexion
                    _uiState.update {
                        it.copy(diagnostico = SesionUiState.Diagnostico.Denegado(mensajeDeSesion(motivo)))
                    }
                }
        }
    }
}
