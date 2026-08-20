package pe.edu.upeu.pharmamobile

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import pe.edu.upeu.pharmamobile.domain.demo.mostrarResultados
import pe.edu.upeu.pharmamobile.domain.result.ResultadoProductos
import pe.edu.upeu.pharmamobile.domain.service.ProductoService

class ProductoFlowAndroidHostTest {

    @Test
    fun evidenciaCorrutinasYFlow() = runBlocking {
        val service = ProductoService()

        println("--- suspend fun obtenerProductos() ---")
        val productos = service.obtenerProductos()
        println("Productos obtenidos: ${productos.size}")
        assertEquals(3, productos.size)

        println("--- Flow<String> observarEstados() ---")
        val estados = mutableListOf<String>()
        service.observarEstados().collect { estado ->
            println("Estado: $estado")
            estados.add(estado)
        }
        assertEquals(listOf("Iniciando", "Finalizado"), estados)

        println("--- Flow<List<Producto>> observarProductos() ---")
        service.observarProductos().collect { lista ->
            println("Emitido: ${lista.size} producto(s)")
        }

        println("--- Flow<ResultadoProductos> cargarProductos() ---")
        var resultadoFinal: ResultadoProductos? = null
        service.cargarProductos().collect { resultado ->
            mostrarResultados(resultado)
            resultadoFinal = resultado
        }
        assertTrue(resultadoFinal is ResultadoProductos.Exito)
    }
}
