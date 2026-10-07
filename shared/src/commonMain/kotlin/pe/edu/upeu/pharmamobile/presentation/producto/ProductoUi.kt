package pe.edu.upeu.pharmamobile.presentation.producto

import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.platform.formatearSoles

/**
 * Modelo listo para pintar: el formato de moneda se resuelve aqui, en la
 * capa de presentacion, ni en el dominio ni dentro del composable.
 */
data class ProductoUi(
    val id: Long,
    val nombre: String,
    val precio: String,
    val stock: Int
)

fun Producto.toUi() = ProductoUi(
    id = id,
    nombre = nombre,
    precio = formatearSoles(precio),
    stock = stock
)
