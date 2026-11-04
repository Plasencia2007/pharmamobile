package pe.edu.upeu.pharmamobile.data.repository

import pe.edu.upeu.pharmamobile.data.mapper.toDomain
import pe.edu.upeu.pharmamobile.data.mapper.toRequest
import pe.edu.upeu.pharmamobile.data.remote.ProductoApi
import pe.edu.upeu.pharmamobile.data.remote.ejecutarLlamada
import pe.edu.upeu.pharmamobile.data.remote.mensajeDe
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobile.domain.model.ErrorSesion
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.model.ResultadoEnvio
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRemotoRepository

/** Productos que se piden al servidor en cada sincronizacion (una sola pagina grande). */
private const val TAMANIO_SINCRONIZACION = 100

/**
 * Traduce cada error de la API a una rama de ResultadoEnvio: este es el unico
 * lugar de la sincronizacion que sabe que existe HTTP.
 *
 * Reintentar un POST cuya respuesta se perdio no duplica nada en PharmaSoft: el
 * servidor rechaza nombres repetidos con 409 y la fila quedaria como fallida,
 * explicando que el nombre ya existe. Ese rechazo hace de clave de operacion.
 */
class EnvioRepositorioRest(
    private val api: ProductoApi,
    private val categoriaPorDefecto: Long
) : ProductoRemotoRepository {

    override suspend fun enviar(producto: Producto): ResultadoEnvio =
        ejecutarLlamada { api.crear(producto.toRequest(categoriaPorDefecto)) }.fold(
            onSuccess = { ResultadoEnvio.Exito(it.id) },
            onFailure = { fallo ->
                (fallo as? ErrorApiException)?.error?.aResultado()
                    ?: ResultadoEnvio.Transitorio(fallo.message ?: "Error inesperado")
            }
        )

    override suspend fun listar(): List<Producto> =
        api.listar(pagina = 0, tamanio = TAMANIO_SINCRONIZACION).contenido.map { it.toDomain() }

    private fun ErrorApi.aResultado(): ResultadoEnvio = when (this) {
        ErrorApi.SinConexion -> ResultadoEnvio.Aplazado(mensajeDe(this))
        ErrorApi.TiempoAgotado, ErrorApi.Servidor -> ResultadoEnvio.Transitorio(mensajeDe(this))
        is ErrorApi.Validacion, ErrorApi.NoEncontrado, is ErrorApi.Conflicto ->
            ResultadoEnvio.Permanente(mensajeDe(this))
        // La sesion vencida se resuelve con un nuevo login y la fila sigue siendo valida;
        // la falta de permiso, en cambio, no la arregla ningun reintento.
        is ErrorApi.DeSesion ->
            if (motivo == ErrorSesion.SinPermiso) ResultadoEnvio.Permanente(mensajeDe(this))
            else ResultadoEnvio.Aplazado(mensajeDe(this))
    }
}
