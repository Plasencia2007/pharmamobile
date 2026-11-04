package pe.edu.upeu.pharmamobile.domain.model

/**
 * Lo que paso al enviar una fila al servidor, sin codigos HTTP: la capa de
 * datos traduce cada error a una de estas ramas y el caso de uso decide.
 */
sealed interface ResultadoEnvio {
    data class Exito(val idServidor: Long) : ResultadoEnvio

    /** Fallo de esta fila que puede pasarse solo (servidor caido, tiempo agotado): gasta un intento. */
    data class Transitorio(val motivo: String) : ResultadoEnvio

    /** El servidor rechazo la fila (validacion, conflicto, sin permiso): reintentarla no sirve. */
    data class Permanente(val motivo: String) : ResultadoEnvio

    /**
     * El problema no es de la fila sino del momento: sin red o con la sesion vencida.
     * No gasta intentos (si no, cinco aperturas sin red darian por fallida una fila
     * sana) y corta la cola, porque las demas filas fallarian igual.
     */
    data class Aplazado(val motivo: String) : ResultadoEnvio
}

/** Balance de una sincronizacion. */
data class ResumenSync(val enviadas: Int, val fallidas: Int, val pendientes: Int)

/** Estado de la cola de envio que se muestra en pantalla. */
data class ColaEnvio(val pendientes: Int = 0, val fallidas: Int = 0)
