package pe.edu.upeu.pharmamobile.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import pe.edu.upeu.pharmamobile.domain.model.Producto

/**
 * Contrato offline-first (Sesion 11): la interfaz lee siempre de la base
 * local mediante observar() y la red es un proceso aparte, sincronizar(),
 * cuya unica mision es mantener esa base al dia.
 */
interface ProductoRepository {

    /** La pantalla se suscribe una vez y se actualiza sola cada vez que la tabla cambia. */
    fun observar(): Flow<List<Producto>>

    /** Lee de la base local; solo consulta al servidor si el producto aun no esta guardado. */
    suspend fun obtener(id: Long): Producto

    /** Guarda en local (queda pendiente) e intenta enviar si hay conexion. */
    suspend fun registrar(producto: Producto): Producto

    /** Edita en el servidor y en local; un producto pendiente solo se edita en local. */
    suspend fun actualizar(producto: Producto): Producto

    /** Borrado logico en el servidor; un producto pendiente simplemente se descarta. */
    suspend fun eliminar(id: Long)

    /** Sube lo pendiente y DESPUES trae la lista del servidor. En ese orden. */
    suspend fun sincronizar()

    /**
     * Cuando termino bien la ultima sincronizacion (milisegundos epoch), o null si
     * nunca se sincronizo. Por defecto null: los repositorios sin base local no la guardan.
     */
    fun observarUltimaSincronizacion(): Flow<Long?> = flowOf(null)
}
