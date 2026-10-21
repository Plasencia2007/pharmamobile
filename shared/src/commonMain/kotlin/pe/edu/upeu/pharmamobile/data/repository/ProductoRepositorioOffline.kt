package pe.edu.upeu.pharmamobile.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobile.data.local.ProductoLocal
import pe.edu.upeu.pharmamobile.data.mapper.toDomain
import pe.edu.upeu.pharmamobile.data.mapper.toRequest
import pe.edu.upeu.pharmamobile.data.remote.ProductoApi
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import kotlin.time.Clock

/** Productos que se piden al servidor en cada sincronizacion (una sola pagina grande). */
private const val TAMANIO_SINCRONIZACION = 100

/**
 * Repositorio offline-first (Sesion 11). La base local es la unica fuente
 * de verdad de la interfaz; el servidor solo se consulta para mantenerla
 * al dia. categoriaPorDefecto cubre la falta de un selector de categoria: el
 * backend exige categoriaId en cada POST/PUT.
 */
class ProductoRepositorioOffline(
    private val local: ProductoLocal,
    private val remoto: ProductoApi,
    private val categoriaPorDefecto: Long,
    /** Inyectable para que las pruebas fijen la hora en lugar de depender del reloj real. */
    private val ahora: () -> Long = { Clock.System.now().toEpochMilliseconds() }
) : ProductoRepository {

    override fun observar(): Flow<List<Producto>> = local.observar()

    override fun observarUltimaSincronizacion(): Flow<Long?> = local.observarUltimaSincronizacion()

    override suspend fun obtener(id: Long): Producto =
        local.obtener(id) ?: remoto.obtener(id).toDomain().also { local.guardarSincronizado(it) }

    override suspend fun registrar(producto: Producto): Producto {
        val guardado = local.guardarPendiente(producto)
        intentar { sincronizar() }
        return guardado
    }

    override suspend fun actualizar(producto: Producto): Producto {
        if (producto.id < 0) {
            // Pendiente: el servidor no lo conoce todavia, se edita solo la fila local.
            return producto.copy(pendiente = true).also { local.guardarEdicionPendiente(it) }
        }
        val actualizado = remoto.actualizar(producto.id, producto.toRequest(categoriaPorDefecto)).toDomain()
        local.guardarSincronizado(actualizado)
        return actualizado
    }

    override suspend fun eliminar(id: Long) {
        if (id < 0) {
            local.borrar(id)
            return
        }
        remoto.eliminar(id)
        local.desactivar(id)
    }

    /** Primero subir, despues bajar: al reves, la lista del servidor borraria lo creado sin red. */
    override suspend fun sincronizar() {
        subirPendientes()
        local.reemplazarSincronizados(
            remoto.listar(pagina = 0, tamanio = TAMANIO_SINCRONIZACION).contenido.map { it.toDomain() }
        )
        // Solo se llega aqui si la subida y la bajada terminaron bien: un fallo a medias
        // lanza antes y conserva la marca anterior, que es la de los datos que se ven.
        local.registrarSincronizacion(ahora())
    }

    private suspend fun subirPendientes() {
        local.pendientes().forEach { pendiente ->
            remoto.crear(pendiente.toRequest(categoriaPorDefecto))
            // El servidor ya lo tiene con su id real y volvera en la lista: la fila local
            // con id negativo sobraria y se reenviaria en cada sincronizacion (duplicados).
            local.borrar(pendiente.id)
        }
    }

    /** Un fallo de red no debe tumbar la operacion local: queda pendiente y se reintenta al sincronizar. */
    private suspend fun intentar(bloque: suspend () -> Unit) {
        try {
            bloque()
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (_: Exception) {
            // Sin conexion, tiempo agotado o rechazo del servidor: el producto sigue pendiente.
        }
    }
}
