package pe.edu.upeu.pharmamobile.data.repository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobile.data.local.ProductoLocal
import pe.edu.upeu.pharmamobile.data.local.baseEnMemoria
import pe.edu.upeu.pharmamobile.data.mapper.toDomain
import pe.edu.upeu.pharmamobile.data.remote.ProductoApi
import pe.edu.upeu.pharmamobile.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobile.data.remote.dto.ProductoResponseDto
import pe.edu.upeu.pharmamobile.domain.model.Producto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Actividad autonoma 11, Producto 1: las seis pruebas que pide el documento,
 * en el mismo orden y con el mismo nombre. Usan la base en memoria y el
 * MockEngine de Ktor: ni emulador, ni red, ni esperas.
 *
 * JUnit crea una instancia nueva de la clase por cada prueba, asi que cada una
 * recibe su propia base y su propio servidor: nada se hereda entre pruebas.
 */
class SuiteOfflineTest {

    private val servidor = ServidorFalso()
    private val local = ProductoLocal(baseEnMemoria())
    private val api = ProductoApi(crearHttpClient(servidor.motor, AlmacenVacio(), esDepuracion = false))
    private val repo = ProductoRepositorioOffline(local, api, categoriaPorDefecto = 1L)

    private val ibuprofeno = Producto(0, "Ibuprofeno 400mg", 4.5, 20)

    private fun enServidor(vararg productos: ProductoResponseDto) {
        servidor.productos += productos
    }

    @Test
    fun `1 sincronizacion con red - la base guarda lo del servidor y el Flow emite la lista completa`() = runTest {
        enServidor(
            ProductoResponseDto(1, "Paracetamol 500mg", 2.0, 50),
            ProductoResponseDto(2, "Amoxicilina 500mg", 22.0, 3),
            ProductoResponseDto(3, "Alcohol en Gel", 8.5, 200)
        )

        repo.sincronizar()

        val emitida = repo.observar().first()
        assertEquals(3, emitida.size)
        assertEquals(setOf(1L, 2L, 3L), emitida.map { it.id }.toSet())
        assertTrue(emitida.none { it.pendiente })
        // No basta con el Flow: la fila tiene que estar en la tabla.
        assertEquals("Amoxicilina 500mg", local.obtener(2)?.nombre)
    }

    @Test
    fun `2 registro sin red - queda guardado como pendiente y el Flow lo emite`() = runTest {
        servidor.conectado = false

        val guardado = repo.registrar(ibuprofeno)

        val fila = assertNotNull(local.obtener(guardado.id), "el producto debe estar en la base local")
        assertTrue(fila.pendiente, "debe quedar marcado como pendiente")
        val emitida = repo.observar().first()
        assertEquals(listOf("Ibuprofeno 400mg"), emitida.map { it.nombre })
        assertTrue(emitida.single().pendiente)
        assertEquals(0, servidor.productos.size, "el servidor no lo recibio")
    }

    @Test
    fun `3 envio al reconectar - lo pendiente llega al servidor y deja de estar marcado`() = runTest {
        servidor.conectado = false
        repo.registrar(ibuprofeno)
        assertEquals(1, local.pendientes().size)

        servidor.conectado = true
        repo.sincronizar()

        assertEquals(listOf("Ibuprofeno 400mg"), servidor.productos.map { it.nombre }, "llego al servidor")
        assertTrue(local.pendientes().isEmpty(), "ninguna fila sigue marcada")
        val fila = repo.observar().first().single()
        assertFalse(fila.pendiente)
        assertEquals(100L, fila.id, "la fila ahora tiene el id real que asigno el servidor")
    }

    @Test
    fun `4 la bajada no borra lo pendiente - traer la lista del servidor conserva lo creado sin red`() = runTest {
        enServidor(ProductoResponseDto(1, "Paracetamol 500mg", 2.0, 50))
        repo.sincronizar()
        servidor.conectado = false
        val pendiente = repo.registrar(ibuprofeno)

        // Se aisla la BAJADA: se trae la lista del servidor y se aplica a la base,
        // sin la subida previa que hace sincronizar().
        servidor.conectado = true
        local.reemplazarSincronizados(api.listar(pagina = 0, tamanio = 100).contenido.map { it.toDomain() })

        val emitida = repo.observar().first()
        assertEquals(setOf("Paracetamol 500mg", "Ibuprofeno 400mg"), emitida.map { it.nombre }.toSet())
        assertTrue(assertNotNull(local.obtener(pendiente.id)).pendiente, "el pendiente sigue ahi y sigue marcado")
    }

    @Test
    fun `5 identificadores sin colision - un producto local y uno del servidor conviven`() = runTest {
        // El servidor numera desde 1; el producto local recibe un id negativo.
        enServidor(ProductoResponseDto(1, "Paracetamol 500mg", 2.0, 50))
        repo.sincronizar()
        servidor.conectado = false

        val localNuevo = repo.registrar(ibuprofeno)

        assertTrue(localNuevo.id < 0, "el id local es negativo: el servidor nunca lo usa")
        val emitida = repo.observar().first()
        assertEquals(2, emitida.size, "ninguno sobrescribio al otro")
        assertEquals("Paracetamol 500mg", local.obtener(1)?.nombre)
        assertEquals("Ibuprofeno 400mg", local.obtener(localNuevo.id)?.nombre)
    }

    @Test
    fun `6 sincronizar dos veces no duplica - la cantidad de filas no cambia`() = runTest {
        enServidor(ProductoResponseDto(1, "Paracetamol 500mg", 2.0, 50))
        servidor.conectado = false
        repo.registrar(ibuprofeno)
        servidor.conectado = true

        repo.sincronizar()
        val despuesDeLaPrimera = repo.observar().first().size
        repo.sincronizar()
        val despuesDeLaSegunda = repo.observar().first().size

        assertEquals(2, despuesDeLaPrimera)
        assertEquals(despuesDeLaPrimera, despuesDeLaSegunda)
        assertEquals(1, servidor.productos.count { it.nombre == "Ibuprofeno 400mg" }, "el servidor lo recibio una sola vez")
    }
}
