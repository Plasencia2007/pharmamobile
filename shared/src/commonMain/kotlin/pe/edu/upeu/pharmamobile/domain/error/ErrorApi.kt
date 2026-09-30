package pe.edu.upeu.pharmamobile.domain.error

/**
 * Representa cada resultado posible de una llamada al backend, sin que el
 * dominio conozca las excepciones de Ktor. ejecutarLlamada() es el unico
 * punto que traduce entre ambos mundos.
 */
sealed interface ErrorApi {
    data class Validacion(val porCampo: Map<String, String>) : ErrorApi
    data object NoEncontrado : ErrorApi
    data class Conflicto(val mensaje: String) : ErrorApi
    data object Servidor : ErrorApi
    data object SinConexion : ErrorApi
    data object TiempoAgotado : ErrorApi
}

class ErrorApiException(val error: ErrorApi) : Exception()
