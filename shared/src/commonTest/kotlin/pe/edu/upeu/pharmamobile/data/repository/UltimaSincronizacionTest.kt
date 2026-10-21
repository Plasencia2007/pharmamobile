package pe.edu.upeu.pharmamobile.data.repository

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import pe.edu.upeu.pharmamobile.data.local.ProductoLocal
import pe.edu.upeu.pharmamobile.data.local.baseEnMemoria
import pe.edu.upeu.pharmamobile.data.remote.ProductoApi
import pe.edu.upeu.pharmamobile.data.remote.crearHttpClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/** Actividad autonoma 11, Producto 3: el instante de la ultima sincronizacion correcta. */
class UltimaSincronizacionTest {

    private val servidor = ServidorFalso()
    private var reloj = 1_000_000L
    private val repo = ProductoRepositorioOffline(
        local = ProductoLocal(baseEnMemoria()),
        remoto = ProductoApi(crearHttpClient(servidor.motor, AlmacenVacio(), esDepuracion = false)),
        categoriaPorDefecto = 1L,
        ahora = { reloj }
    )

    @Test
    fun `una base nueva nunca se sincronizo`() = runTest {
        assertNull(repo.observarUltimaSincronizacion().first())
    }

    @Test
    fun `una sincronizacion correcta guarda su instante y la siguiente lo reemplaza`() = runTest {
        repo.sincronizar()
        assertEquals(1_000_000L, repo.observarUltimaSincronizacion().first())

        reloj = 5_000_000L
        repo.sincronizar()
        assertEquals(5_000_000L, repo.observarUltimaSincronizacion().first())
    }

    @Test
    fun `una sincronizacion fallida conserva la marca anterior`() = runTest {
        repo.sincronizar()
        reloj = 9_000_000L
        servidor.conectado = false

        assertFailsWith<IOException> { repo.sincronizar() }

        assertEquals(1_000_000L, repo.observarUltimaSincronizacion().first(), "los datos que se ven siguen siendo los de antes")
    }
}
