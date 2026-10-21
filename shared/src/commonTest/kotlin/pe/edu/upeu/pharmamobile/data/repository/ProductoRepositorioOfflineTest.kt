package pe.edu.upeu.pharmamobile.data.repository

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobile.data.local.ProductoLocal
import pe.edu.upeu.pharmamobile.data.local.baseEnMemoria
import pe.edu.upeu.pharmamobile.data.remote.ProductoApi
import pe.edu.upeu.pharmamobile.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobile.data.remote.dto.PaginaResponseDto
import pe.edu.upeu.pharmamobile.data.remote.dto.ProductoRequestDto
import pe.edu.upeu.pharmamobile.data.remote.dto.ProductoResponseDto
import pe.edu.upeu.pharmamobile.domain.model.Producto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class AlmacenVacio : AlmacenSeguro {
    override fun guardarToken(token: String) = Unit
    override fun leerToken(): String? = null
    override fun borrarToken() = Unit
}

/** PharmaSoft simulado: registra el orden de las llamadas y puede "perder la red". */
private class ServidorFalso {
    var conectado = true
    val llamadas = mutableListOf<String>()
    val productos = mutableListOf<ProductoResponseDto>()
    private var siguienteId = 100L
    private val json = headersOf(HttpHeaders.ContentType, "application/json")

    val motor = MockEngine { peticion ->
        if (!conectado) throw IOException("Sin red")
        val ruta = peticion.url.encodedPath
        when {
            peticion.method == HttpMethod.Post && ruta.endsWith("/productos") -> {
                llamadas += "POST"
                val cuerpo = Json.decodeFromString<ProductoRequestDto>((peticion.body as TextContent).text)
                val nuevo = ProductoResponseDto(siguienteId++, cuerpo.nombre, cuerpo.precio, cuerpo.stock, cuerpo.estado)
                productos += nuevo
                respond(Json.encodeToString(ProductoResponseDto.serializer(), nuevo), HttpStatusCode.Created, json)
            }
            peticion.method == HttpMethod.Get && ruta.endsWith("/productos") -> {
                llamadas += "GET"
                val pagina = PaginaResponseDto(productos.toList(), 0, 100, productos.size.toLong(), 1, true)
                respond(
                    Json.encodeToString(PaginaResponseDto.serializer(ProductoResponseDto.serializer()), pagina),
                    HttpStatusCode.OK, json
                )
            }
            else -> respondError(HttpStatusCode.NotFound)
        }
    }
}

class ProductoRepositorioOfflineTest {

    private val servidor = ServidorFalso()

    /** Una base NUEVA por prueba (ver baseEnMemoria). */
    private val local = ProductoLocal(baseEnMemoria())
    private val repo = ProductoRepositorioOffline(
        local = local,
        remoto = ProductoApi(crearHttpClient(servidor.motor, AlmacenVacio(), esDepuracion = false)),
        categoriaPorDefecto = 1L
    )

    private val ibuprofeno = Producto(0, "Ibuprofeno", 4.5, 20)

    @Test
    fun `sin red el producto queda pendiente y la pantalla lo ve`() = runTest {
        servidor.conectado = false

        repo.registrar(ibuprofeno)

        val lista = repo.observar().first()
        assertEquals(1, lista.size)
        assertTrue(lista.single().pendiente)
        assertTrue(lista.single().id < 0, "el id local debe ser negativo")
    }

    @Test
    fun `con red el producto se envia y queda sincronizado con su id real`() = runTest {
        repo.registrar(ibuprofeno)

        val lista = repo.observar().first()
        assertEquals(1, lista.size, "no debe haber duplicados: ni la fila local ni la del servidor repetidas")
        assertEquals(100L, lista.single().id)
        assertEquals(false, lista.single().pendiente)
    }

    @Test
    fun `sincronizar sube lo pendiente antes de bajar del servidor`() = runTest {
        servidor.conectado = false
        repo.registrar(ibuprofeno)
        servidor.conectado = true
        servidor.llamadas.clear()

        repo.sincronizar()

        assertEquals(listOf("POST", "GET"), servidor.llamadas)
    }

    @Test
    fun `lo creado sin red sobrevive a la bajada del servidor y llega al servidor al reconectar`() = runTest {
        servidor.productos += ProductoResponseDto(1L, "Paracetamol", 2.0, 50)
        servidor.conectado = false
        repo.registrar(ibuprofeno)
        assertEquals(1, repo.observar().first().size)

        servidor.conectado = true
        repo.sincronizar()

        val lista = repo.observar().first()
        assertEquals(setOf("Paracetamol", "Ibuprofeno"), lista.map { it.nombre }.toSet())
        assertTrue(lista.none { it.pendiente })
        assertTrue(lista.all { it.id > 0 }, "todos con id real asignado por el servidor")
        assertEquals(1, servidor.productos.count { it.nombre == "Ibuprofeno" }, "el servidor lo recibio una sola vez")
    }

    @Test
    fun `sincronizar dos veces no vuelve a enviar lo que ya subio`() = runTest {
        servidor.conectado = false
        repo.registrar(ibuprofeno)
        servidor.conectado = true

        repo.sincronizar()
        servidor.llamadas.clear()
        repo.sincronizar()

        assertEquals(listOf("GET"), servidor.llamadas)
        assertEquals(1, repo.observar().first().size)
    }

    @Test
    fun `si la subida falla el pendiente se conserva`() = runTest {
        servidor.conectado = false
        repo.registrar(ibuprofeno)

        assertFailsWith<IOException> { repo.sincronizar() }

        assertEquals(1, local.pendientes().size)
        assertEquals(1, repo.observar().first().size)
    }

    @Test
    fun `dos productos sin red reciben ids locales negativos distintos`() = runTest {
        servidor.conectado = false

        val a = repo.registrar(ibuprofeno)
        val b = repo.registrar(Producto(0, "Amoxicilina", 9.0, 10))

        assertEquals(-1L, a.id)
        assertEquals(-2L, b.id)
    }

    @Test
    fun `reemplazar sincronizados no borra los pendientes`() = runTest {
        local.guardarPendiente(ibuprofeno)

        local.reemplazarSincronizados(listOf(Producto(7, "Paracetamol", 2.0, 50)))

        val lista = local.observar().first()
        assertEquals(setOf("Ibuprofeno", "Paracetamol"), lista.map { it.nombre }.toSet())
        assertEquals(1, local.pendientes().size)
    }

    @Test
    fun `un producto pendiente se edita y se descarta sin llamar al servidor`() = runTest {
        servidor.conectado = false
        val pendiente = repo.registrar(ibuprofeno)

        repo.actualizar(pendiente.copy(nombre = "Ibuprofeno 400"))
        assertEquals("Ibuprofeno 400", repo.observar().first().single().nombre)
        assertTrue(repo.observar().first().single().pendiente)

        repo.eliminar(pendiente.id)
        assertEquals(0, repo.observar().first().size)
        assertTrue(servidor.llamadas.isEmpty())
    }

    @Test
    fun `obtener lee de la base local sin pedirlo al servidor`() = runTest {
        servidor.conectado = false
        val pendiente = repo.registrar(ibuprofeno)

        val leido = repo.obtener(pendiente.id)

        assertEquals("Ibuprofeno", leido.nombre)
        assertNull(local.obtener(9999L))
    }
}
