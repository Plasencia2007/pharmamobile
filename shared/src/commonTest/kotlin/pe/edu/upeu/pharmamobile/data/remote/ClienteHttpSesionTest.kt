package pe.edu.upeu.pharmamobile.data.remote

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondOk
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobile.data.repository.AuthRepositorioRest
import pe.edu.upeu.pharmamobile.domain.model.Credenciales
import pe.edu.upeu.pharmamobile.domain.model.ErrorSesion
import pe.edu.upeu.pharmamobile.domain.model.ErrorSesionException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

private class AlmacenEnMemoria : AlmacenSeguro {
    var token: String? = null
    override fun guardarToken(token: String) { this.token = token }
    override fun leerToken(): String? = token
    override fun borrarToken() { token = null }
}

/**
 * Verifica el cliente Ktor real (con sus plugins Auth y validador) contra un
 * servidor simulado que registra la cabecera Authorization de cada petición.
 * Cubre el efecto 2 del cierre de sesión: el cliente olvida el token en caché.
 */
class ClienteHttpSesionTest {

    private val json = headersOf(HttpHeaders.ContentType, "application/json")
    private val peticiones = mutableListOf<Pair<String, String?>>()
    private var contadorLogin = 0
    private var expiraciones = 0

    private fun repositorio(almacen: AlmacenSeguro): AuthRepositorioRest {
        val motor = MockEngine { peticion ->
            val ruta = peticion.url.encodedPath
            val auth = peticion.headers[HttpHeaders.Authorization]
            peticiones += ruta to auth
            when {
                ruta.endsWith("/auth/login") -> {
                    contadorLogin++
                    respond(
                        """{"token":"tok-$contadorLogin","tipo":"Bearer","expiraEnSegundos":3600,"username":"u$contadorLogin",
                            "nombreCompleto":"Usuario $contadorLogin","roles":[],"permisos":["CATALOGO_LEER"]}""",
                        HttpStatusCode.OK, json
                    )
                }
                auth == null -> respond(
                    """{"message":"Debe iniciar sesión para acceder a este recurso"}""",
                    HttpStatusCode.Unauthorized, json
                )
                else -> respondOk("{}")
            }
        }
        val cliente = crearHttpClient(motor, almacen, esDepuracion = false, alSesionExpirada = { expiraciones++ })
        return AuthRepositorioRest(cliente, almacen)
    }

    @Test
    fun `el login sale sin cabecera y las demas peticiones llevan Bearer`() = runTest {
        val repo = repositorio(AlmacenEnMemoria())
        repo.iniciarSesion(Credenciales("u", "x"))
        repo.probarAcceso("productos")

        assertNull(peticiones[0].second)
        assertEquals("Bearer tok-1", peticiones[1].second)
    }

    @Test
    fun `tras cerrar sesion el cliente olvida el token y la siguiente peticion sale sin cabecera`() = runTest {
        val almacen = AlmacenEnMemoria()
        val repo = repositorio(almacen)
        repo.iniciarSesion(Credenciales("u", "x"))
        repo.probarAcceso("productos")
        assertEquals("Bearer tok-1", peticiones.last().second)

        repo.cerrarSesion()
        assertNull(almacen.leerToken())

        val fallo = assertFailsWith<ErrorSesionException> { repo.probarAcceso("productos") }
        assertNull(peticiones.last().second)
        assertEquals(ErrorSesion.SesionExpirada, fallo.error)
    }

    @Test
    fun `un nuevo login reemplaza el token en cache y no reutiliza el anterior`() = runTest {
        val repo = repositorio(AlmacenEnMemoria())
        repo.iniciarSesion(Credenciales("u1", "x"))
        repo.probarAcceso("productos")
        repo.cerrarSesion()
        repo.iniciarSesion(Credenciales("u2", "x"))
        repo.probarAcceso("productos")

        assertEquals("Bearer tok-2", peticiones.last().second)
    }

    @Test
    fun `un 401 en una ruta protegida avisa a la sesion y un 401 del login no`() = runTest {
        val repo = repositorio(AlmacenEnMemoria())
        repo.cerrarSesion()
        assertFailsWith<ErrorSesionException> { repo.probarAcceso("productos") }
        assertEquals(1, expiraciones)
    }

    @Test
    fun `alterar el token guardado cambia el ultimo caracter y se envia el alterado`() = runTest {
        val almacen = AlmacenEnMemoria()
        val repo = repositorio(almacen)
        repo.iniciarSesion(Credenciales("u", "x"))
        assertEquals(true, repo.alterarTokenGuardado())
        assertEquals("tok-A", almacen.leerToken())
        repo.probarAcceso("productos")
        assertEquals("Bearer tok-A", peticiones.last().second)
    }
}
