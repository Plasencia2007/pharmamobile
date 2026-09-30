package pe.edu.upeu.pharmamobile.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Contrato del backend de práctica (https://api.escuelajs.co/api/v1/products).
 * Solo modela los campos que PharmaMobil necesita; el resto del JSON se
 * ignora gracias a ignoreUnknownKeys = true en HttpClientFactory.
 */
@Serializable
data class ProductoDto(
    val id: Int,
    val title: String,
    val price: Double,
    val description: String = "",
    val images: List<String> = emptyList(),
    @SerialName("category") val categoria: CategoriaDto? = null
)

@Serializable
data class CategoriaDto(val id: Int, val name: String)
