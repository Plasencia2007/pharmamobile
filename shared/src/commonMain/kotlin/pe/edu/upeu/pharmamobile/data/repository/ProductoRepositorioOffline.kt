package pe.edu.upeu.pharmamobile.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import pe.edu.upeu.pharmamobile.data.local.ProductoLocal
import pe.edu.upeu.pharmamobile.data.mapper.toDomain
import pe.edu.upeu.pharmamobile.data.mapper.toRequest
import pe.edu.upeu.pharmamobile.data.remote.ProductoApi
import pe.edu.upeu.pharmamobile.domain.model.ColaEnvio
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.model.ResumenSync
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobile.domain.usecase.SincronizarUseCase
import kotlin.time.Clock

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

    private val sincronizacion = SincronizarUseCase(local, EnvioRepositorioRest(remoto, categoriaPorDefecto), ahora)

    override fun observar(): Flow<List<Producto>> = local.observar()

    override fun observarUltimaSincronizacion(): Flow<Long?> = local.observarUltimaSincronizacion()

    override fun observarCola(): Flow<ColaEnvio> = combine(
        local.observarPendientes(SincronizarUseCase.MAX_INTENTOS),
        local.observarFallidas(SincronizarUseCase.MAX_INTENTOS)
    ) { pendientes, fallidas -> ColaEnvio(pendientes, fallidas) }

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

    /** La logica de la cola vive en SincronizarUseCase; aqui solo se devuelve su balance. */
    override suspend fun sincronizar(): ResumenSync = sincronizacion()

    override suspend fun reintentarFallidas() {
        local.reiniciarFallidas(SincronizarUseCase.MAX_INTENTOS)
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
