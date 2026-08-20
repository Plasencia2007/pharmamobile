package pe.edu.upeu.pharmamobile.domain.service

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pe.edu.upeu.pharmamobile.data.repository.ProductoRepository
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.result.ResultadoProductos

class ProductoService(
    private val repository: ProductoRepository = ProductoRepository()
) {

    suspend fun obtenerProductos(): List<Producto> = repository.obtenerProductos()

    fun observarEstados(): Flow<String> = flow {
        emit("Iniciando")
        delay(1000)
        emit("Finalizado")
    }

    fun observarProductos(): Flow<List<Producto>> = repository.observarProductos()

    fun cargarProductos(): Flow<ResultadoProductos> = repository.cargarProductos()

    suspend fun buscarPorId(id: Long): Producto? {
        delay(300)
        return repository.buscarPorId(id)
    }
}
