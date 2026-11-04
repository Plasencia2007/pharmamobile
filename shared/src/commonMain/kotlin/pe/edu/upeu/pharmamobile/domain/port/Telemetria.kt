package pe.edu.upeu.pharmamobile.domain.port

/**
 * Lo que la app necesita para registrar fallos. El dominio no sabe que por
 * debajo hay Firebase: si cambia el proveedor, solo cambia su implementacion.
 *
 * Se registra contexto tecnico (pantalla, operacion, codigo de estado); nunca
 * datos personales ni el token de sesion: esto termina en un servicio externo.
 */
interface Telemetria {
    fun registrarError(e: Throwable, contexto: Map<String, String> = emptyMap())
    fun registrarEvento(nombre: String, datos: Map<String, String> = emptyMap())
}

/** Para las pruebas y para quien no necesita telemetria. */
object TelemetriaNula : Telemetria {
    override fun registrarError(e: Throwable, contexto: Map<String, String>) = Unit
    override fun registrarEvento(nombre: String, datos: Map<String, String>) = Unit
}
