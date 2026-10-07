package pe.edu.upeu.pharmamobile.presentation.detalle

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobile.data.remote.mensajeDe
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobile.domain.platform.Compartidor
import pe.edu.upeu.pharmamobile.domain.usecase.ObtenerProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.comoTextoParaCompartir
import pe.edu.upeu.pharmamobile.presentation.producto.toUi

/**
 * Detalle de un producto. Recibe el Compartidor por su interfaz de dominio:
 * no sabe si debajo hay un Intent de Android o un UIActivityViewController.
 */
class DetalleProductoViewModel(
    private val productoId: Long,
    private val obtenerProducto: ObtenerProductoUseCase,
    private val compartidor: Compartidor
) : ViewModel() {

    var uiState: DetalleProductoUiState by mutableStateOf(DetalleProductoUiState.Cargando)
        private set

    init {
        cargar()
    }

    fun reintentar() = cargar()

    fun compartir() {
        val estado = uiState as? DetalleProductoUiState.Contenido ?: return
        compartidor.compartir(estado.producto.comoTextoParaCompartir())
    }

    private fun cargar() {
        uiState = DetalleProductoUiState.Cargando
        viewModelScope.launch {
            obtenerProducto(productoId)
                .onSuccess { uiState = DetalleProductoUiState.Contenido(it, it.toUi()) }
                .onFailure { fallo ->
                    val error = (fallo as? ErrorApiException)?.error
                    uiState = DetalleProductoUiState.Error(mensajeDe(error))
                }
        }
    }
}
