package pe.edu.upeu.pharmamobile.domain.repository

import pe.edu.upeu.pharmamobile.domain.model.Credenciales
import pe.edu.upeu.pharmamobile.domain.model.Sesion

interface AuthRepository {
    suspend fun iniciarSesion(credenciales: Credenciales): Sesion
    suspend fun sesionGuardada(): Sesion?
    suspend fun cerrarSesion()

    /** Consulta un recurso protegido solo para observar la respuesta del servidor (diagnóstico de permisos). */
    suspend fun probarAcceso(recurso: String)

    /** Solo depuración: cambia un carácter del token guardado. Devuelve false si no había token. */
    suspend fun alterarTokenGuardado(): Boolean
}
