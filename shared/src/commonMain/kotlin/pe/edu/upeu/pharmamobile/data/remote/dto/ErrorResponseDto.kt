package pe.edu.upeu.pharmamobile.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Cuerpo que PharmaSoft devuelve en las respuestas 4xx/5xx
 * (GlobalExceptionHandler.ErrorResponseDTO). timestamp/status/error no se
 * necesitan en la app: solo interesan el mensaje y, si el fallo fue de
 * validacion, el detalle por campo.
 */
@Serializable
data class ErrorResponseDto(
    val message: String? = null,
    val validationErrors: Map<String, String>? = null
)
