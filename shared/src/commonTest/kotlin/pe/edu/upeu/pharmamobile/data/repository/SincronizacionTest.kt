package pe.edu.upeu.pharmamobile.data.repository

import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobile.data.local.ProductoLocal
import pe.edu.upeu.pharmamobile.data.local.baseEnMemoria
import pe.edu.upeu.pharmamobile.data.remote.ProductoApi
import pe.edu.upeu.pharmamobile.data.remote.crearHttpClient
import pe.edu.upeu.pharmamobile.data.remote.dto.ProductoResponseDto
import pe.edu.upeu.pharmamobile.domain.model.ColaEnvio
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.port.Telemetria
import pe.edu.upeu.pharmamobile.domain.usecase.SincronizarUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class TelemetriaFalsa : Telemetria {
    val eventos = mutableListOf<Pair<String, Map<String, String>>>()
    override fun registrarError(e: Throwable, contexto: Map<String, String>) = Unit
    override fun registrarEvento(nombre: String, datos: Map<String, String>) {
        eventos += nombre to datos
    }
}

/** Guia 13, paso 7: la cola de envio con la base en memoria y el motor simulado. */
class SincronizacionTest {

    private val servidor = ServidorFalso()
    private val local = ProductoLocal(baseEnMemoria())
    private val api = ProductoApi(crearHttpClient(servidor.motor, AlmacenVacio(), esDepuracion = false))
    private val telemetria = TelemetriaFalsa()
    private val esperas = mutableListOf<Long>()
    private val sincronizar = SincronizarUseCase(
        local, EnvioRepositorioRest(api, categoriaPorDefecto = 1L), ahora = { 1L }, telemetria,
        esperar = { esperas += it }
    )
    private val repo = ProductoRepositorioOffline(local, api, categoriaPorDefecto = 1L)

    private fun pendiente(nombre: String = "Ibuprofeno 400mg"): Producto =
        local.guardarPendiente(Producto(0, nombre, 4.5, 20))

    @Test
    fun unFalloTransitorioSumaIntentoYNoMarcaFallida() = runTest {
        val fila = pendiente()
        servidor.errorEnPost = HttpStatusCode.ServiceUnavailable

        sincronizar()

        val guardada = assertNotNull(local.obtener(fila.id))
        assertEquals(1, guardada.intentos)
        assertTrue(guardada.pendiente)
        assertFalse(guardada.envioFallido())
        assertNotNull(guardada.ultimoError, "se recuerda que fallo la ultima vez")
    }

    @Test
    fun unFalloPermanenteMarcaLaFilaYNoLaReintenta() = runTest {
        val fila = pendiente()
        servidor.errorEnPost = HttpStatusCode.Conflict

        sincronizar()
        servidor.llamadas.clear()
        sincronizar()

        assertTrue(assertNotNull(local.obtener(fila.id)).envioFallido(), "queda fallida y visible")
        assertEquals(listOf("GET"), servidor.llamadas, "no se vuelve a enviar")
        assertEquals(ColaEnvio(pendientes = 0, fallidas = 1), repo.observarCola().first())
    }

    @Test
    fun unaFilaRechazadaNoBloqueaALasDemasNiLaBajada() = runTest {
        servidor.productos += ProductoResponseDto(1, "Paracetamol 500mg", 2.0, 50)
        servidor.rechazarNombre = "Repetido"
        pendiente("Repetido")
        pendiente("Amoxicilina 500mg")

        val resumen = sincronizar()

        assertEquals(1, resumen.enviadas)
        assertEquals(1, resumen.fallidas)
        val nombres = repo.observar().first().map { it.nombre }.toSet()
        assertEquals(setOf("Paracetamol 500mg", "Amoxicilina 500mg", "Repetido"), nombres)
    }

    @Test
    fun laEsperaCreceYAlAgotarLosIntentosLaFilaQuedaFallida() = runTest {
        val fila = pendiente()
        servidor.errorEnPost = HttpStatusCode.InternalServerError

        repeat(SincronizarUseCase.MAX_INTENTOS + 2) { sincronizar() }

        assertEquals(listOf(1_000L, 2_000L, 4_000L, 8_000L, 16_000L), esperas)
        assertEquals(SincronizarUseCase.MAX_INTENTOS, assertNotNull(local.obtener(fila.id)).intentos)
        assertEquals(ColaEnvio(pendientes = 0, fallidas = 1), repo.observarCola().first())
    }

    @Test
    fun unServidorQueNoRespondeGastaIntentosPorqueSiHayRed() = runTest {
        val fila = pendiente()
        servidor.sinRespuesta = true

        runCatching { sincronizar() }

        assertEquals(1, assertNotNull(local.obtener(fila.id)).intentos)
        assertEquals(listOf(1_000L), esperas)
    }

    @Test
    fun sinRedNoSeGastanIntentos() = runTest {
        val fila = pendiente()
        servidor.conectado = false

        repeat(8) { runCatching { sincronizar() } }

        val guardada = assertNotNull(local.obtener(fila.id))
        assertEquals(0, guardada.intentos, "ocho aperturas sin red no dan por fallida una fila sana")
        assertTrue(esperas.isEmpty(), "sin red no se espera entre intentos")
    }

    @Test
    fun laSesionVencidaNoGastaIntentosYLaFilaSeEnviaDespues() = runTest {
        val fila = pendiente()
        servidor.errorEnPost = HttpStatusCode.Unauthorized

        sincronizar()
        assertEquals(0, assertNotNull(local.obtener(fila.id)).intentos)

        servidor.errorEnPost = null
        sincronizar()

        assertNull(local.obtener(fila.id), "ya no existe el id local")
        assertEquals(listOf("Ibuprofeno 400mg"), servidor.productos.map { it.nombre })
    }

    @Test
    fun faltaDePermisoEsPermanente() = runTest {
        val fila = pendiente()
        servidor.errorEnPost = HttpStatusCode.Forbidden

        sincronizar()

        assertTrue(assertNotNull(local.obtener(fila.id)).envioFallido())
    }

    @Test
    fun reintentarDevuelveLasFallidasALaCola() = runTest {
        val fila = pendiente()
        servidor.errorEnPost = HttpStatusCode.Conflict
        sincronizar()

        repo.reintentarFallidas()
        servidor.errorEnPost = null
        sincronizar()

        assertNull(local.obtener(fila.id))
        assertEquals(ColaEnvio(), repo.observarCola().first())
    }

    @Test
    fun corregirUnaFilaFallidaLaDevuelveALaCola() = runTest {
        val fila = pendiente()
        servidor.errorEnPost = HttpStatusCode.Conflict
        sincronizar()

        repo.actualizar(assertNotNull(local.obtener(fila.id)).copy(nombre = "Ibuprofeno 600mg"))

        assertEquals(ColaEnvio(pendientes = 1, fallidas = 0), repo.observarCola().first())
    }

    @Test
    fun laTelemetriaNoRecibeLoQueEscribioElUsuario() = runTest {
        pendiente("Dato del usuario")
        servidor.errorEnPost = HttpStatusCode.Conflict

        sincronizar()

        assertEquals(listOf("sync_fila_rechazada" to emptyMap()), telemetria.eventos)
    }
}
