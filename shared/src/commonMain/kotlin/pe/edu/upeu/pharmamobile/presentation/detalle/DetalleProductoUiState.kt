package pe.edu.upeu.pharmamobile.presentation.detalle

import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoUi

sealed interface DetalleProductoUiState {
    data object Cargando : DetalleProductoUiState
    data class Contenido(val producto: Producto, val ui: ProductoUi) : DetalleProductoUiState
    data class Error(val mensaje: String) : DetalleProductoUiState
}
