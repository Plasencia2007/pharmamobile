package pe.edu.upeu.pharmamobile.presentation.producto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobile.domain.usecase.ProductoValidationException
import pe.edu.upeu.pharmamobile.domain.usecase.RegistrarProductoUseCase

class ProductoViewModel(
    private val registrarProductoUseCase: RegistrarProductoUseCase,
    private val productoRepository: ProductoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductoUiState())
    val uiState: StateFlow<ProductoUiState> = _uiState.asStateFlow()

    init {
        cargarProductos()
    }

    fun cargarProductos() {
        viewModelScope.launch {
            _uiState.update { it.copy(fase = ProductoUiState.FaseInventario.Cargando) }
            runCatching { productoRepository.listar() }
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
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            fase = ProductoUiState.FaseInventario.Error(
                                error.message ?: "No se pudo cargar el inventario"
                            )
                        )
                    }
                }
        }
    }

    fun registrarProducto(nombre: String, precio: String, stock: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    formulario = it.formulario.copy(
                        enviando = true,
                        errorNombre = null,
                        errorPrecio = null,
                        errorStock = null
                    )
                )
            }
            registrarProductoUseCase(nombre, precio, stock)
                .onSuccess {
                    _uiState.update {
                        it.copy(formulario = ProductoUiState.FormularioState(mensajeExito = "Producto registrado en el inventario"))
                    }
                    cargarProductos()
                }
                .onFailure { error ->
                    val validacion = error as? ProductoValidationException
                    _uiState.update {
                        it.copy(
                            formulario = it.formulario.copy(
                                enviando = false,
                                errorNombre = validacion?.errorNombre,
                                errorPrecio = validacion?.errorPrecio,
                                errorStock = validacion?.errorStock
                            )
                        )
                    }
                }
        }
    }

    fun consumirMensajeExito() {
        _uiState.update { it.copy(formulario = it.formulario.copy(mensajeExito = null)) }
    }
}
