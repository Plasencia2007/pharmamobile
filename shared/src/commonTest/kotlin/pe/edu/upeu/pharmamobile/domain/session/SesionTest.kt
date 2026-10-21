package pe.edu.upeu.pharmamobile.domain.session

import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import pe.edu.upeu.pharmamobile.data.remote.mapearErrorSesion
import pe.edu.upeu.pharmamobile.data.remote.mensajeDe
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.model.Credenciales
import pe.edu.upeu.pharmamobile.domain.model.ErrorSesion
import pe.edu.upeu.pharmamobile.domain.model.ErrorSesionException
import pe.edu.upeu.pharmamobile.domain.model.Permisos
import pe.edu.upeu.pharmamobile.domain.model.Sesion
import pe.edu.upeu.pharmamobile.domain.repository.AuthRepository
import pe.edu.upeu.pharmamobile.domain.usecase.IniciarSesionUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.validarCredenciales
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private val sesionAlmacen = Sesion(
    token = "tok",
    usuario = "almacen",
    nombreCompleto = "Jorge Mendoza Huaman",
    permisos = setOf("CATALOGO_LEER", "CATALOGO_ESCRIBIR")
)

/** Repositorio falso: guarda el "token" en memoria, como lo haría el almacén. */
private class AuthFalso(
    var guardada: Sesion? = null,
    var fallo: ErrorSesion? = null
) : AuthRepository {
    var cierres = 0

    override suspend fun iniciarSesion(credenciales: Credenciales): Sesion {
        fallo?.let { throw ErrorSesionException(it) }
        guardada = sesionAlmacen
        return sesionAlmacen
    }

    override suspend fun sesionGuardada(): Sesion? = guardada

    override suspend fun cerrarSesion() {
        cierres++
        guardada = null
    }

    override suspend fun probarAcceso(recurso: String) = Unit

    override suspend fun alterarTokenGuardado() = false
}

class SesionTest {

    @Test
    fun `puede responde segun el arreglo de permisos`() {
        assertTrue(sesionAlmacen.puede(Permisos.CATALOGO_ESCRIBIR))
        assertFalse(sesionAlmacen.puede(Permisos.CLIENTES_LEER))
    }

    @Test
    fun `401 en el login es credenciales invalidas y en una ruta protegida es sesion expirada`() {
        assertEquals(ErrorSesion.CredencialesInvalidas, mapearErrorSesion(HttpStatusCode.Unauthorized, enLogin = true))
        assertEquals(ErrorSesion.SesionExpirada, mapearErrorSesion(HttpStatusCode.Unauthorized, enLogin = false))
    }

    @Test
    fun `403 es falta de permiso y su mensaje es distinto al de sesion expirada`() {
        assertEquals(ErrorSesion.SinPermiso, mapearErrorSesion(HttpStatusCode.Forbidden, enLogin = false))
        val permiso = mensajeDe(ErrorApi.DeSesion(ErrorSesion.SinPermiso))
        val expirada = mensajeDe(ErrorApi.DeSesion(ErrorSesion.SesionExpirada))
        assertTrue(permiso != expirada)
    }

    @Test
    fun `validarCredenciales marca los campos vacios`() {
        val errores = validarCredenciales("  ", "")
        assertNotNull(errores.usuario)
        assertNotNull(errores.clave)
        assertFalse(validarCredenciales("admin", "Admin123").hayErrores)
    }

    @Test
    fun `iniciar sesion correcta devuelve la sesion y credenciales malas fallan con ErrorSesion`() = runTest {
        val bueno = IniciarSesionUseCase(AuthFalso())("almacen", "Almacen123")
        assertEquals(sesionAlmacen, bueno.getOrNull())

        val malo = IniciarSesionUseCase(AuthFalso(fallo = ErrorSesion.CredencialesInvalidas))("almacen", "x")
        assertEquals(ErrorSesion.CredencialesInvalidas, (malo.exceptionOrNull() as ErrorSesionException).error)
    }

    @Test
    fun `restaurar recupera la sesion guardada sin pedir credenciales`() = runTest {
        val gestor = GestorSesion(AuthFalso(guardada = sesionAlmacen))
        assertTrue(gestor.restaurando.value)
        gestor.restaurar()
        assertEquals(sesionAlmacen, gestor.sesion.value)
        assertFalse(gestor.restaurando.value)
    }

    @Test
    fun `expirar borra el token y limpia el estado y deja un aviso`() = runTest {
        val auth = AuthFalso(guardada = sesionAlmacen)
        val gestor = GestorSesion(auth)
        gestor.restaurar()

        gestor.expirar()

        assertNull(gestor.sesion.value)
        assertNull(auth.guardada)
        assertEquals(AVISO_SESION_EXPIRADA, gestor.aviso.value)
    }

    @Test
    fun `expirar sin sesion y sin restauracion en curso no deja aviso`() = runTest {
        val gestor = GestorSesion(AuthFalso())
        gestor.restaurar()
        gestor.expirar()
        assertNull(gestor.aviso.value)
    }

    @Test
    fun `un login nuevo descarta el aviso de sesion expirada`() = runTest {
        val gestor = GestorSesion(AuthFalso())
        gestor.expirar()
        gestor.iniciar(sesionAlmacen)
        assertNull(gestor.aviso.value)
        assertEquals(sesionAlmacen, gestor.sesion.value)
    }
}
