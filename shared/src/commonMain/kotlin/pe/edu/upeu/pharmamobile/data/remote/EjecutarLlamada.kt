package pe.edu.upeu.pharmamobile.data.remote

import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import pe.edu.upeu.pharmamobile.data.remote.dto.ErrorResponseDto
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException

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
    null -> "Ocurrió un error inesperado."
}
