package pe.edu.upeu.pharmamobile.presentation.producto

import pe.edu.upeu.pharmamobile.domain.model.Producto

data class ProductoUiState(
    val fase: FaseInventario = FaseInventario.Cargando,
    val formulario: FormularioState = FormularioState()
) {
    sealed interface FaseInventario {
        data object Cargando : FaseInventario
        data object SinProductos : FaseInventario
        data class ConProductos(val productos: List<Producto>) : FaseInventario
        data class Error(val mensaje: String) : FaseInventario
    }

    data class FormularioState(
        val enviando: Boolean = false,
        val errorNombre: String? = null,
        val errorPrecio: String? = null,
        val errorStock: String? = null,
        val mensajeExito: String? = null
    )
}
