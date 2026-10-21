package pe.edu.upeu.pharmamobile.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import kotlin.time.Clock
import pe.edu.upeu.pharmamobile.data.local.AlmacenSeguro
import pe.edu.upeu.pharmamobile.data.remote.dto.LoginRequestDto
import pe.edu.upeu.pharmamobile.data.remote.dto.LoginResponseDto
import pe.edu.upeu.pharmamobile.data.remote.dto.UsuarioActualDto
import pe.edu.upeu.pharmamobile.data.remote.mapearErrorSesion
import pe.edu.upeu.pharmamobile.data.remote.sesionDesdeToken
import pe.edu.upeu.pharmamobile.domain.model.Credenciales
import pe.edu.upeu.pharmamobile.domain.model.ErrorSesion
import pe.edu.upeu.pharmamobile.domain.model.ErrorSesionException
import pe.edu.upeu.pharmamobile.domain.model.Sesion
import pe.edu.upeu.pharmamobile.domain.repository.AuthRepository

class AuthRepositorioRest(
    private val cliente: HttpClient,
    private val almacen: AlmacenSeguro
) : AuthRepository {

    override suspend fun iniciarSesion(credenciales: Credenciales): Sesion {
        val dto: LoginResponseDto = llamar(enLogin = true) {
            cliente.post("auth/login") {
                setBody(LoginRequestDto(credenciales.usuario, credenciales.clave))
            }.body()
        }
        almacen.guardarToken(dto.token)
        // loadTokens quedó en caché con el valor anterior: sin esto, la
        // primera sesión del día funciona y las siguientes no.
        olvidarTokenEnCache()
        return dto.aSesion()
    }

    /**
     * Reconstruye la sesión desde el token del almacén consultando /auth/me.
     * Si el servidor responde 401 el token ya no sirve y se descarta.
     * Sin conexión devuelve null pero conserva el token para el próximo intento.
     */
    override suspend fun sesionGuardada(): Sesion? {
        val token = almacen.leerToken() ?: return null
        return try {
            llamar<UsuarioActualDto>(enLogin = false) { cliente.get("auth/me").body() }.aSesion(token)
        } catch (e: ErrorSesionException) {
            when (e.error) {
                ErrorSesion.SesionExpirada -> {
                    cerrarSesion()
                    null
                }
                // Offline-first: sin conexion la app abre igual, con la sesion que dice el propio token
                // (si ya vencio, devuelve null y se pide iniciar sesion). El servidor valida cada peticion.
                ErrorSesion.SinConexion -> sesionDesdeToken(token, Clock.System.now().epochSeconds)
                else -> null
            }
        }
    }

    override suspend fun cerrarSesion() {
        almacen.borrarToken()
        olvidarTokenEnCache()
    }

    override suspend fun probarAcceso(recurso: String) {
        llamar<Unit>(enLogin = false) {
            cliente.get(recurso)
            Unit
        }
    }

    override suspend fun alterarTokenGuardado(): Boolean {
        val token = almacen.leerToken() ?: return false
        val ultimo = token.last()
        almacen.guardarToken(token.dropLast(1) + if (ultimo == 'A') 'B' else 'A')
        olvidarTokenEnCache()
        return true
    }

    private fun olvidarTokenEnCache() {
        cliente.authProvider<BearerAuthProvider>()?.clearToken()
    }

    /** Traduce las excepciones de Ktor a ErrorSesionException en un único lugar. */
    private suspend fun <T> llamar(enLogin: Boolean, bloque: suspend () -> T): T =
        try {
            bloque()
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (e: ClientRequestException) {
            throw ErrorSesionException(mapearErrorSesion(e.response.status, enLogin))
        } catch (e: ServerResponseException) {
            throw ErrorSesionException(ErrorSesion.SinConexion)
        } catch (e: HttpRequestTimeoutException) {
            throw ErrorSesionException(ErrorSesion.SinConexion)
        } catch (e: IOException) {
            throw ErrorSesionException(ErrorSesion.SinConexion)
        }
}
