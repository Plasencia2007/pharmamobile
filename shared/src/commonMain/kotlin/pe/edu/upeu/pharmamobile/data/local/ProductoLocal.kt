package pe.edu.upeu.pharmamobile.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upeu.pharmamobile.db.PharmaDb
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.db.Producto as FilaProducto

/**
 * Capa local: concentra las consultas y traduce entre las filas generadas
 * por SQLDelight y el modelo de dominio. No conoce Ktor ni los DTO del
 * backend, ni el backend a ella.
 */
class ProductoLocal(base: PharmaDb) {

    private val consultas = base.productoQueries

    /** La pantalla se suscribe una vez: emite de nuevo cada vez que la tabla cambia. */
    fun observar(): Flow<List<Producto>> =
        consultas.listarTodos()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { filas -> filas.map { it.aDominio() } }

    fun obtener(id: Long): Producto? =
        consultas.obtenerPorId(id).executeAsOneOrNull()?.aDominio()

    fun pendientes(): List<Producto> =
        consultas.listarPendientes().executeAsList()
            .map { it.aDominio() }

    /**
     * Guarda lo que el usuario creo sin conexion con un id local NEGATIVO: un
     * id 0 podria chocar con uno del servidor y sobrescribirlo, y los
     * negativos reservan un espacio que el backend nunca ocupa.
     */
    fun guardarPendiente(producto: Producto): Producto =
        consultas.transactionWithResult {
            val menor = consultas.siguienteIdLocal().executeAsOne()
            val local = producto.copy(id = minOf(menor, 0L) - 1L, pendiente = true)
            insertar(local)
            local
        }

    /** Edicion de un producto que sigue pendiente: conserva su id negativo y su marca. */
    fun guardarEdicionPendiente(producto: Producto) = insertar(producto.copy(pendiente = true))

    /** Fila que el servidor ya conoce (alta, edicion o respuesta del servidor). */
    fun guardarSincronizado(producto: Producto) = insertar(producto.copy(pendiente = false))

    /** Una vez que el servidor acepto un pendiente, su fila local con id negativo deja de existir. */
    fun borrar(id: Long) = consultas.borrarPorId(id)

    /** El borrado de PharmaSoft es logico: el producto sigue existiendo, inactivo. */
    fun desactivar(id: Long) = consultas.desactivar(id)

    /**
     * Recibe la lista del servidor. Borra SOLO las filas ya sincronizadas
     * (pendiente = 0): borrar la tabla entera haria que la bajada arrasara
     * con lo que el usuario creo sin red. Todo va en una transaccion: sin
     * ella el Flow emitiria una vez por fila insertada y la lista parpadearia.
     */
    fun reemplazarSincronizados(productos: List<Producto>) {
        consultas.transaction {
            consultas.borrarSincronizados()
            productos.forEach { insertar(it.copy(pendiente = false)) }
        }
    }

    private fun insertar(p: Producto) =
        consultas.insertar(
            id = p.id,
            nombre = p.nombre,
            precio = p.precio,
            stock = p.stock.toLong(),
            activo = if (p.activo) 1L else 0L,
            pendiente = if (p.pendiente) 1L else 0L
        )

    private fun FilaProducto.aDominio() = Producto(
        id = id,
        nombre = nombre,
        precio = precio,
        stock = stock.toInt(),
        activo = activo == 1L,
        pendiente = pendiente == 1L
    )
}
