package pe.edu.upeu.pharmamobile.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Espejo de PaginaResponseDTO<T> del backend: el listado de productos
 * llega envuelto en esta estructura de paginacion, no como un array suelto.
 */
@Serializable
data class PaginaResponseDto<T>(
    val contenido: List<T>,
    val pagina: Int,
    val tamanio: Int,
    val totalElementos: Long,
    val totalPaginas: Int,
    val ultima: Boolean
)
