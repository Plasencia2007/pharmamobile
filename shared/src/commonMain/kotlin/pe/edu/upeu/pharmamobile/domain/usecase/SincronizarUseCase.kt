package pe.edu.upeu.pharmamobile.domain.usecase

import kotlinx.coroutines.delay
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.model.ResultadoEnvio
import pe.edu.upeu.pharmamobile.domain.model.ResumenSync
import pe.edu.upeu.pharmamobile.domain.port.Telemetria
import pe.edu.upeu.pharmamobile.domain.port.TelemetriaNula
import pe.edu.upeu.pharmamobile.domain.repository.ProductoLocalRepository
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRemotoRepository

/**
 * Sube la cola y despues baja la lista (sesion 13). Una fila rechazada ya no
 * detiene la sincronizacion: queda marcada y las demas siguen su camino. Si la
 * bajada falla, la excepcion llega a quien llamo y la marca de tiempo no cambia.
 */
class SincronizarUseCase(
    private val local: ProductoLocalRepository,
    private val remoto: ProductoRemotoRepository,
    private val ahora: () -> Long,
    private val telemetria: Telemetria = TelemetriaNula,
    private val esperar: suspend (Long) -> Unit = { delay(it) }
) {
    suspend operator fun invoke(): ResumenSync {
        var enviadas = 0
        var fallidas = 0
        for (fila in local.pendientes(MAX_INTENTOS)) {
            when (val resultado = remoto.enviar(fila)) {
                is ResultadoEnvio.Exito -> {
                    local.confirmar(fila.id, resultado.idServidor)
                    enviadas++
                }
                is ResultadoEnvio.Permanente -> {
                    local.marcarFallida(fila.id, resultado.motivo, MAX_INTENTOS)
                    // Sin el motivo: repite lo que escribio el usuario y no debe salir del telefono.
                    telemetria.registrarEvento("sync_fila_rechazada")
                    fallidas++
                }
                is ResultadoEnvio.Transitorio -> {
                    local.sumarIntento(fila.id, resultado.motivo)
                    esperar(espera(fila.intentos))
                }
                is ResultadoEnvio.Aplazado -> break
            }
        }
        // Solo despues de subir se baja: invertirlo borraria lo pendiente.
        local.conciliar(remoto.listar())
        local.registrarSincronizacion(ahora())
        return ResumenSync(enviadas, fallidas, local.contarPendientes(MAX_INTENTOS))
    }

    /** 1 s, 2 s, 4 s, 8 s, 16 s: una red intermitente no castiga la bateria. */
    private fun espera(intentos: Int): Long = 1_000L shl intentos.coerceAtMost(4)

    companion object {
        const val MAX_INTENTOS = Producto.MAX_INTENTOS_ENVIO
    }
}
