package pe.edu.upeu.pharmamobile.data.remote.dto

import kotlinx.serialization.Serializable
import pe.edu.upeu.pharmamobile.domain.model.Sesion

@Serializable
data class LoginRequestDto(val username: String, val password: String)

@Serializable
data class LoginResponseDto(
    val token: String,
    val tipo: String,
    val expiraEnSegundos: Long,
    val username: String,
    val nombreCompleto: String,
    val roles: List<String>,
    val permisos: List<String>
) {
    fun aSesion() = Sesion(token, username, nombreCompleto, permisos.toSet())
}

/** Respuesta de GET /auth/me: no repite el token, que ya está en el almacén. */
@Serializable
data class UsuarioActualDto(
    val username: String,
    val empleadoNombre: String? = null,
    val roles: List<String> = emptyList(),
    val permisos: List<String> = emptyList()
) {
    fun aSesion(token: String) = Sesion(token, username, empleadoNombre ?: username, permisos.toSet())
}
