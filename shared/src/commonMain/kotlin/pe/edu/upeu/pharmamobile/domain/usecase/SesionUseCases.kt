package pe.edu.upeu.pharmamobile.domain.usecase

import kotlinx.coroutines.CancellationException
import pe.edu.upeu.pharmamobile.domain.model.Credenciales
import pe.edu.upeu.pharmamobile.domain.model.Sesion
import pe.edu.upeu.pharmamobile.domain.repository.AuthRepository

/** Errores por campo del formulario de login; null significa "campo válido". */
data class ErroresCredenciales(val usuario: String?, val clave: String?) {
    val hayErrores: Boolean get() = usuario != null || clave != null
}

fun validarCredenciales(usuario: String, clave: String) = ErroresCredenciales(
    usuario = if (usuario.isBlank()) "Ingresa tu usuario" else null,
    clave = if (clave.isBlank()) "Ingresa tu contraseña" else null
)

class IniciarSesionUseCase(private val repositorio: AuthRepository) {
    suspend operator fun invoke(usuario: String, clave: String): Result<Sesion> =
        try {
            Result.success(repositorio.iniciarSesion(Credenciales(usuario.trim(), clave)))
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (e: Exception) {
            Result.failure(e)
        }
}

class CerrarSesionUseCase(private val repositorio: AuthRepository) {
    suspend operator fun invoke() = repositorio.cerrarSesion()
}

/** Diagnóstico de permisos: pide un recurso protegido y devuelve lo que respondió el servidor. */
class ProbarAccesoUseCase(private val repositorio: AuthRepository) {
    suspend operator fun invoke(recurso: String): Result<Unit> =
        try {
            Result.success(repositorio.probarAcceso(recurso))
        } catch (cancelacion: CancellationException) {
            throw cancelacion
        } catch (e: Exception) {
            Result.failure(e)
        }
}
