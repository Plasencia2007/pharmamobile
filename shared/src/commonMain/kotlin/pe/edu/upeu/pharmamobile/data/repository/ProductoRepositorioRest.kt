package pe.edu.upeu.pharmamobile.data.repository

import pe.edu.upeu.pharmamobile.data.mapper.toDomain
import pe.edu.upeu.pharmamobile.data.mapper.toRequest
import pe.edu.upeu.pharmamobile.data.remote.ProductoApi
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

/**
 * Implementacion conectada de ProductoRepository (Sesion 8): las cinco
 * operaciones llaman directamente a ProductoApi. Las excepciones de Ktor
 * no se atrapan aqui -- se propagan tal cual hasta los casos de uso, que
 * las traducen con ejecutarLlamada(). categoriaPorDefecto cubre la falta
 * de un selector de categoria real en el formulario: el backend exige
 * categoriaId en cada POST/PUT, y esta app aun no tiene pantalla de
 * categorias (queda para una sesion posterior).
 */
class ProductoRepositorioRest(
    private val api: ProductoApi,
    private val categoriaPorDefecto: Long
) : ProductoRepository {

    override suspend fun listar(): List<Producto> =
        api.listar().contenido.map { it.toDomain() }

    override suspend fun obtener(id: Long): Producto =
        api.obtener(id).toDomain()

    override suspend fun registrar(producto: Producto): Producto =
        api.crear(producto.toRequest(categoriaPorDefecto)).toDomain()

    override suspend fun actualizar(producto: Producto): Producto =
        api.actualizar(producto.id, producto.toRequest(categoriaPorDefecto)).toDomain()

    override suspend fun eliminar(id: Long) = api.eliminar(id)
}
