package pe.edu.upeu.pharmamobile.domain.error

import pe.edu.upeu.pharmamobile.domain.model.ErrorSesion

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

    /** 401 (sesión vencida o ausente) o 403 (falta de permiso) en una ruta protegida. */
    data class DeSesion(val motivo: ErrorSesion) : ErrorApi
}

class ErrorApiException(val error: ErrorApi) : Exception()
