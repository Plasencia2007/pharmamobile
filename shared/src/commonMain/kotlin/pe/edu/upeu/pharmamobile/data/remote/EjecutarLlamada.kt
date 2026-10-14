package pe.edu.upeu.pharmamobile.data.remote

import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import pe.edu.upeu.pharmamobile.data.remote.dto.ErrorResponseDto
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobile.domain.model.ErrorSesion

/**
 * Unico punto donde una excepcion de Ktor se traduce a ErrorApi. El resto
 * del codigo (repositorio, casos de uso, ViewModel) solo conoce Result<T>
 * y ErrorApiException; nunca las excepciones concretas del cliente HTTP.
 */
suspend fun <T> ejecutarLlamada(bloque: suspend () -> T): Result<T> =
    try {
        Result.success(bloque())
    } catch (cancelacion: CancellationException) {
        throw cancelacion
    } catch (e: ErrorApiException) {
        // Defensivo: si el bloque ya lanzo un ErrorApi tipificado (por
        // ejemplo un repositorio falso en pruebas), no se re-traduce.
        Result.failure(e)
    } catch (e: ClientRequestException) {
        Result.failure(ErrorApiException(traducirCliente(e)))
    } catch (e: ServerResponseException) {
        Result.failure(ErrorApiException(ErrorApi.Servidor))
    } catch (e: HttpRequestTimeoutException) {
        Result.failure(ErrorApiException(ErrorApi.TiempoAgotado))
    } catch (e: IOException) {
        Result.failure(ErrorApiException(ErrorApi.SinConexion))
    }

private suspend fun traducirCliente(e: ClientRequestException): ErrorApi {
    val cuerpo = runCatching { e.response.body<ErrorResponseDto>() }.getOrNull()
    return when (e.response.status.value) {
        401, 403 -> ErrorApi.DeSesion(mapearErrorSesion(e.response.status, enLogin = false))
        400 -> ErrorApi.Validacion(cuerpo?.validationErrors.orEmpty())
        404 -> ErrorApi.NoEncontrado
        409 -> ErrorApi.Conflicto(cuerpo?.message ?: "Operación no permitida")
        else -> ErrorApi.Servidor
    }
}

/** Mensaje legible para mostrar en la UI cuando no aplica un campo puntual. */
fun mensajeDe(error: ErrorApi?): String = when (error) {
    is ErrorApi.Validacion -> error.porCampo.values.firstOrNull() ?: "Revisa los datos ingresados."
    ErrorApi.NoEncontrado -> "El producto ya no existe."
    is ErrorApi.Conflicto -> error.mensaje
    ErrorApi.Servidor -> "El servidor no pudo procesar la solicitud. Intenta nuevamente."
    ErrorApi.SinConexion -> "No hay conexión con el servidor."
    ErrorApi.TiempoAgotado -> "La solicitud tardó demasiado. Intenta nuevamente."
    is ErrorApi.DeSesion -> mensajeDeSesion(error.motivo)
    null -> "Ocurrió un error inesperado."
}

/**
 * Código HTTP de seguridad a ErrorSesion. En una ruta protegida un 401 es
 * siempre sesión vencida o inválida; solo en el login significa credenciales
 * incorrectas. Un 403 es falta de permiso y NO afecta a la sesión.
 */
fun mapearErrorSesion(estado: HttpStatusCode, enLogin: Boolean): ErrorSesion = when (estado) {
    HttpStatusCode.Unauthorized ->
        if (enLogin) ErrorSesion.CredencialesInvalidas else ErrorSesion.SesionExpirada
    HttpStatusCode.Forbidden -> ErrorSesion.SinPermiso
    else -> ErrorSesion.SinConexion
}

fun mensajeDeSesion(error: ErrorSesion): String = when (error) {
    ErrorSesion.CredencialesInvalidas -> "Usuario o contraseña incorrectos."
    ErrorSesion.SesionExpirada -> "Tu sesión expiró o ya no es válida. Inicia sesión nuevamente."
    ErrorSesion.SinPermiso -> "No tienes permiso para realizar esta operación."
    ErrorSesion.SinConexion -> "No hay conexión con el servidor."
}
