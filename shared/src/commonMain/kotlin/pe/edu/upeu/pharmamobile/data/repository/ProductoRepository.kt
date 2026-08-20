package pe.edu.upeu.pharmamobile.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.result.ResultadoProductos

class ProductoRepository {

    private val productosSimulados = listOf(
        Producto(id = 1L, nombre = "Paracetamol", precio = 8.50, stock = 100),
        Producto(id = 2L, nombre = "Ibuprofeno", precio = 12.00, stock = 50),
        Producto(id = 3L, nombre = "Amoxicilina", precio = 18.50, stock = 20)
    )

    fun listarTodos(): List<Producto> = productosSimulados

    fun productosDisponibles(): List<Producto> = productosSimulados.filter { it.stock > 0 }

    fun nombresProductos(): List<String> = productosSimulados.map { it.nombre }

    fun buscarPorId(id: Long): Producto? = productosSimulados.find { it.id == id }

    suspend fun obtenerProductos(): List<Producto> {
        delay(1000) // Simula espera de red
        return productosSimulados
    }

    fun observarProductos(): Flow<List<Producto>> = flow {
        emit(emptyList())
        delay(1000)
        emit(productosSimulados)
    }

    fun cargarProductos(): Flow<ResultadoProductos> = flow {
        emit(ResultadoProductos.Cargando)
        delay(1000)
        emit(ResultadoProductos.Exito(productosSimulados))
    }
}
