package pe.edu.upeu.pharmamobile.presentation.producto

import pe.edu.upeu.pharmamobile.domain.model.ColaEnvio
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.presentation.sincronizacion.SincronizacionUiState

data class ProductoUiState(
    val fase: FaseInventario = FaseInventario.Cargando,
    val formulario: FormularioState = FormularioState(),
    val operacion: Operacion = Operacion.Inactiva,
    /** Hay una sincronizacion con PharmaSoft en curso (segundo plano; la lista sigue visible). */
    val sincronizando: Boolean = false,
    /** La ultima sincronizacion fallo: se siguen mostrando los datos guardados. */
    val avisoRed: String? = null,
    /** Ultima sincronizacion correcta (milisegundos epoch); null = este telefono nunca sincronizo. */
    val ultimaSincronizacion: Long? = null,
    /** Cuantas filas faltan por enviar y cuantas fallaron, segun la base local. */
    val cola: ColaEnvio = ColaEnvio()
) {
    /** Productos creados en el dispositivo que siguen en la cola de envio. */
    val pendientes: Int get() = cola.pendientes

    val estadoSincronizacion: SincronizacionUiState
        get() = SincronizacionUiState(sincronizando, cola.pendientes, cola.fallidas, ultimaSincronizacion, avisoRed)

    sealed interface FaseInventario {
        data object Cargando : FaseInventario
        data object SinProductos : FaseInventario
        data class ConProductos(val productos: List<Producto>) : FaseInventario
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
