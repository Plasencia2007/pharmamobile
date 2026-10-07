package pe.edu.upeu.pharmamobile.presentation.producto

import pe.edu.upeu.pharmamobile.domain.model.Producto

data class ProductoUiState(
    val fase: FaseInventario = FaseInventario.Cargando,
    val formulario: FormularioState = FormularioState(),
    val operacion: Operacion = Operacion.Inactiva
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

    /** Distingue "cargando la lista" de "guardando/eliminando un producto". */
    sealed interface Operacion {
        data object Inactiva : Operacion
        data class EnCurso(val tipo: Tipo, val productoId: Long? = null) : Operacion
        data class Fallida(val mensaje: String) : Operacion
        enum class Tipo { Crear, Actualizar, Eliminar }
    }
}
