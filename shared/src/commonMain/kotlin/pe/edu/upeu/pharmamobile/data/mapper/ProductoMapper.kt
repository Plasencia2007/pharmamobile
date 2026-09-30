package pe.edu.upeu.pharmamobile.data.mapper

import pe.edu.upeu.pharmamobile.data.remote.dto.ProductoDto
import pe.edu.upeu.pharmamobile.domain.model.Producto

/**
 * La API de práctica (escuelajs) no modela inventario de farmacia: no trae
 * stock ni un indicador de "activo". STOCK_DE_REFERENCIA deja el producto
 * visible en la pestaña "Activos" sin fingir una cifra real de existencias.
 */
private const val STOCK_DE_REFERENCIA = 20

fun ProductoDto.toDomain(): Producto = Producto(
    id = id.toLong(),
    nombre = title,
    precio = price,
    stock = STOCK_DE_REFERENCIA,
    activo = true,
    descripcion = description,
    imagenUrl = images.firstOrNull() ?: "",
    categoriaProducto = categoria?.name ?: "Sin categoría"
)
