package pe.edu.upeu.pharmamobile.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobile.domain.result.ResultadoProductos

class ProductoRepositorioEnMemoria : ProductoRepository {

    // Datos de la Sesión 3 (corrutinas y Flow): se conservan tal cual para
    // no romper ProductoService ni ProductoFlowAndroidHostTest.
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

    // --- Contrato de dominio ProductoRepository (Reto 01: Clean + MVVM) ---
    // Inventario que respalda la pantalla de Productos. El id lo asigna este
    // repositorio, nunca la pantalla ni el caso de uso.

    private val inventario = mutableListOf(
        Producto(id = 1L, nombre = "Paracetamol", precio = 15.50, stock = 100, activo = true),
        Producto(id = 2L, nombre = "Ibuprofeno", precio = 18.90, stock = 50, activo = true),
        Producto(id = 3L, nombre = "Amoxicilina", precio = 25.00, stock = 5, activo = true),
        Producto(id = 4L, nombre = "Loratadina", precio = 12.50, stock = 0, activo = false),
        Producto(id = 5L, nombre = "Diclofenaco", precio = 20.00, stock = 3, activo = true)
    )
    private var siguienteId = (inventario.maxOfOrNull { it.id } ?: 0L) + 1

    override suspend fun registrar(producto: Producto): Producto {
        delay(500) // Estado de carga visible al registrar
        val productoConId = producto.copy(id = siguienteId)
        siguienteId += 1
        inventario.add(productoConId)
        return productoConId
    }

    override suspend fun listar(): List<Producto> {
        delay(500) // Estado de carga visible al listar
        return inventario.toList()
    }
}
