package pe.edu.upeu.pharmamobile.data.remote.dto

import kotlinx.serialization.Serializable

/** Cuerpo que se envia en POST/PUT /api/v1/productos. */
@Serializable
data class ProductoRequestDto(
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val estado: Boolean = true,
    val categoriaId: Long
)

/**
 * Contrato del backend PharmaSoft para cada producto (GET por id, y cada
 * elemento del listado paginado). fechaCreacion/fechaModificacion se
 * omiten: con ignoreUnknownKeys = true en HttpClientFactory, la app no se
 * rompe por no declararlas.
 */
@Serializable
data class ProductoResponseDto(
    val id: Long,
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val estado: Boolean = true,
    val categoriaId: Long? = null,
    val categoriaNombre: String? = null
)
