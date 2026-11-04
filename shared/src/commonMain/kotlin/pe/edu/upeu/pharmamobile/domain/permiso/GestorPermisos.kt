package pe.edu.upeu.pharmamobile.domain.permiso

enum class Permiso { CAMARA, UBICACION, NOTIFICACIONES }

sealed interface EstadoPermiso {
    data object Concedido : EstadoPermiso

    /** Se puede volver a pedir. */
    data object Denegado : EstadoPermiso

    /** El sistema ya no muestra el dialogo: solo queda ir a Ajustes. */
    data object DenegadoParaSiempre : EstadoPermiso
}

/**
 * Es una interfaz porque pedir un permiso necesita piezas de cada plataforma
 * (en Android, la Activity) que el codigo comun no puede nombrar. Las pruebas
 * le pasan una implementacion falsa.
 */
interface GestorPermisos {
    suspend fun estado(permiso: Permiso): EstadoPermiso
    suspend fun solicitar(permiso: Permiso): EstadoPermiso
    fun abrirAjustes()
}
