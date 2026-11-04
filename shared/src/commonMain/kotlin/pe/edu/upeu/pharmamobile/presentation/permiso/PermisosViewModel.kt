package pe.edu.upeu.pharmamobile.presentation.permiso

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobile.domain.permiso.EstadoPermiso
import pe.edu.upeu.pharmamobile.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobile.domain.permiso.Permiso

data class PermisosUiState(
    /** null hasta la primera consulta. */
    val notificaciones: EstadoPermiso? = null,
    /** Se esta mostrando por que se necesita el permiso, antes del dialogo del sistema. */
    val explicando: Boolean = false,
    /** El usuario quiere las alertas; sin permiso no tiene efecto. */
    val alertasElegidas: Boolean = true
) {
    val alertasActivas: Boolean
        get() = alertasElegidas && notificaciones == EstadoPermiso.Concedido
}

/** Las alertas de stock bajo son el uso real del permiso: se pide al activarlas, nunca al abrir la app. */
class PermisosViewModel(private val gestor: GestorPermisos) : ViewModel() {

    private val _uiState = MutableStateFlow(PermisosUiState())
    val uiState: StateFlow<PermisosUiState> = _uiState.asStateFlow()

    /** Se llama al volver a la app: el usuario pudo cambiar el permiso desde Ajustes. */
    fun refrescar() {
        viewModelScope.launch {
            val estado = gestor.estado(Permiso.NOTIFICACIONES)
            _uiState.update { it.copy(notificaciones = estado) }
        }
    }

    fun alCambiarAlertas(activar: Boolean) {
        when {
            !activar -> _uiState.update { it.copy(alertasElegidas = false) }
            _uiState.value.notificaciones == EstadoPermiso.Concedido ->
                _uiState.update { it.copy(alertasElegidas = true) }
            _uiState.value.notificaciones == EstadoPermiso.DenegadoParaSiempre -> gestor.abrirAjustes()
            else -> _uiState.update { it.copy(explicando = true) }
        }
    }

    fun confirmarExplicacion() {
        _uiState.update { it.copy(explicando = false) }
        viewModelScope.launch {
            val estado = gestor.solicitar(Permiso.NOTIFICACIONES)
            _uiState.update {
                it.copy(notificaciones = estado, alertasElegidas = estado == EstadoPermiso.Concedido)
            }
        }
    }

    fun cancelarExplicacion() {
        _uiState.update { it.copy(explicando = false) }
    }

    fun abrirAjustes() = gestor.abrirAjustes()
}
