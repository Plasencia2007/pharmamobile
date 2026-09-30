package pe.edu.upeu.pharmamobile.presentation.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobile.data.remote.mensajeDe
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.ListarProductosUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.ProductoValidationException
import pe.edu.upeu.pharmamobile.domain.usecase.RegistrarProductoUseCase

class ProductoViewModel(
    private val listarProductosUseCase: ListarProductosUseCase,
    private val registrarProductoUseCase: RegistrarProductoUseCase,
    private val actualizarProductoUseCase: ActualizarProductoUseCase,
    private val eliminarProductoUseCase: EliminarProductoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductoUiState())
    val uiState: StateFlow<ProductoUiState> = _uiState.asStateFlow()

    init {
        cargarProductos()
    }

    fun cargarProductos() {
        viewModelScope.launch {
            _uiState.update { it.copy(fase = ProductoUiState.FaseInventario.Cargando) }
            listarProductosUseCase()
                .onSuccess { productos ->
                    _uiState.update {
                        it.copy(
                            fase = if (productos.isEmpty()) {
                                ProductoUiState.FaseInventario.SinProductos
                            } else {
                                ProductoUiState.FaseInventario.ConProductos(productos)
                            }
                        )
                    }
                }
                .onFailure { fallo ->
                    _uiState.update {
                        it.copy(fase = ProductoUiState.FaseInventario.Error(mensajeDeFallo(fallo)))
                    }
                }
        }
    }

    fun registrarProducto(nombre: String, precio: String, stock: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    operacion = ProductoUiState.Operacion.EnCurso(ProductoUiState.Operacion.Tipo.Crear),
                    formulario = it.formulario.copy(
                        enviando = true, errorNombre = null, errorPrecio = null, errorStock = null
                    )
                )
            }
            registrarProductoUseCase(nombre, precio, stock)
                .onSuccess {
                    cargarProductos()
                    _uiState.update {
                        it.copy(
                            operacion = ProductoUiState.Operacion.Inactiva,
                            formulario = ProductoUiState.FormularioState(mensajeExito = "Producto registrado en el inventario")
                        )
                    }
                }
                .onFailure { fallo -> manejarFalloFormulario(fallo) }
        }
    }

    fun actualizarProducto(producto: Producto, nombre: String, precio: String, stock: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    operacion = ProductoUiState.Operacion.EnCurso(ProductoUiState.Operacion.Tipo.Actualizar, producto.id),
                    formulario = it.formulario.copy(
                        enviando = true, errorNombre = null, errorPrecio = null, errorStock = null
                    )
                )
            }
            actualizarProductoUseCase(producto, nombre, precio, stock)
                .onSuccess {
                    cargarProductos()
                    _uiState.update {
                        it.copy(
                            operacion = ProductoUiState.Operacion.Inactiva,
                            formulario = ProductoUiState.FormularioState(mensajeExito = "Producto actualizado")
                        )
                    }
                }
                .onFailure { fallo -> manejarFalloFormulario(fallo) }
        }
    }

    fun eliminarProducto(id: Long) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(operacion = ProductoUiState.Operacion.EnCurso(ProductoUiState.Operacion.Tipo.Eliminar, id))
            }
            eliminarProductoUseCase(id)
                .onSuccess {
                    cargarProductos()
                    _uiState.update {
                        it.copy(
                            operacion = ProductoUiState.Operacion.Inactiva,
                            formulario = it.formulario.copy(mensajeExito = "Producto eliminado")
                        )
                    }
                }
                .onFailure { fallo ->
                    _uiState.update {
                        it.copy(operacion = ProductoUiState.Operacion.Fallida(mensajeDeFallo(fallo)))
                    }
                }
        }
    }

    fun consumirMensajeExito() {
        _uiState.update { it.copy(formulario = it.formulario.copy(mensajeExito = null)) }
    }

    fun consumirOperacionFallida() {
        _uiState.update { it.copy(operacion = ProductoUiState.Operacion.Inactiva) }
    }

    private fun manejarFalloFormulario(fallo: Throwable) {
        val validacion = fallo as? ProductoValidationException
        val error = (fallo as? ErrorApiException)?.error
        when {
            validacion != null -> _uiState.update {
                it.copy(
                    operacion = ProductoUiState.Operacion.Inactiva,
                    formulario = it.formulario.copy(
                        enviando = false,
                        errorNombre = validacion.errorNombre,
                        errorPrecio = validacion.errorPrecio,
                        errorStock = validacion.errorStock
                    )
                )
            }
            error is ErrorApi.Validacion -> _uiState.update {
                it.copy(
                    operacion = ProductoUiState.Operacion.Inactiva,
                    formulario = it.formulario.copy(
                        enviando = false,
                        errorNombre = error.porCampo["nombre"],
                        errorPrecio = error.porCampo["precio"],
                        errorStock = error.porCampo["stock"]
                    )
                )
            }
            else -> _uiState.update {
                it.copy(
                    operacion = ProductoUiState.Operacion.Fallida(mensajeDe(error)),
                    formulario = it.formulario.copy(enviando = false)
                )
            }
        }
    }

    private fun mensajeDeFallo(fallo: Throwable): String {
        val error = (fallo as? ErrorApiException)?.error
        return error?.let { mensajeDe(it) } ?: (fallo.message ?: "No se pudo cargar el inventario")
    }
}
