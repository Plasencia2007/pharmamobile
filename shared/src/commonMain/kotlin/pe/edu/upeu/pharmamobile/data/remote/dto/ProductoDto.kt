package pe.edu.upeu.pharmamobile.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Contrato del backend PharmaSoft (GET /api/v1/productos), tal como lo
 * expone ProductoResponseDTO en el servidor. fechaCreacion/fechaModificacion
 * se ignoran (ignoreUnknownKeys = true en HttpClientFactory): esta práctica
 * solo necesita listar el inventario.
 */
@Serializable
data class ProductoDto(
    val id: Long,
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val estado: Boolean,
    val categoriaId: Long? = null,
    val categoriaNombre: String? = null
)

/**
 * Espejo de PaginaResponseDTO<T> del backend: el listado de productos
 * llega envuelto en esta estructura de paginación, no como un array suelto.
 */
@Serializable
data class PaginaDto<T>(
    val contenido: List<T>,
    val pagina: Int,
    val tamanio: Int,
    val totalElementos: Long,
    val totalPaginas: Int,
    val ultima: Boolean
)
