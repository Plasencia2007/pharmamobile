package pe.edu.upeu.pharmamobile.data.repository

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pe.edu.upeu.pharmamobile.data.mapper.toDomain
import pe.edu.upeu.pharmamobile.data.remote.ProductoApi
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

/**
 * Implementación conectada de ProductoRepository (Sesión 7): listar() hace
 * una petición GET real contra la API de práctica y mapea cada ProductoDto
 * al dominio. La guía solo cubre el consumo GET -- el endpoint de creación
 * (POST) llega en una sesión posterior del CRUD -- así que registrar() por
 * ahora guarda el producto en memoria con un id negativo (para no chocar
 * con los ids reales de la API) y listar() lo antepone a la respuesta del
 * servidor, para que el formulario del Reto 01 siga siendo funcional.
 */
class ProductoRepositoryRemoto(
    private val api: ProductoApi
) : ProductoRepository {

    private val mutex = Mutex()
    private val registradosLocalmente = mutableListOf<Producto>()
    private var siguienteIdLocal = -1L

    override suspend fun registrar(producto: Producto): Producto = mutex.withLock {
        val productoConId = producto.copy(id = siguienteIdLocal)
        siguienteIdLocal -= 1
        registradosLocalmente.add(0, productoConId)
        productoConId
    }

    override suspend fun listar(): List<Producto> {
        val remotos = api.obtenerProductos().contenido.map { it.toDomain() }
        val locales = mutex.withLock { registradosLocalmente.toList() }
        return locales + remotos
    }
}
