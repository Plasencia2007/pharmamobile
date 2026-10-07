package pe.edu.upeu.pharmamobile.domain.usecase

class ProductoValidationException(
    val errorNombre: String?,
    val errorPrecio: String?,
    val errorStock: String?
) : Exception("Los datos del producto no son válidos")

/** Reglas de validación compartidas entre registrar y actualizar. */
internal object ValidacionProducto {

    fun validar(nombre: String, precio: String, stock: String): ProductoValidationException? {
        val errorNombre = validarNombre(nombre)
        val errorPrecio = validarPrecio(precio)
        val errorStock = validarStock(stock)
        return if (errorNombre != null || errorPrecio != null || errorStock != null) {
            ProductoValidationException(errorNombre, errorPrecio, errorStock)
        } else {
            null
        }
    }

    private fun validarNombre(nombre: String): String? =
        if (nombre.isBlank()) "El nombre es obligatorio." else null

    private fun validarPrecio(precio: String): String? {
        val valor = precio.toDoubleOrNull()
        return when {
            valor == null -> "Ingrese un precio numérico."
            valor <= 0 -> "El precio debe ser mayor que cero."
            else -> null
        }
    }

    private fun validarStock(stock: String): String? {
        val valor = stock.toIntOrNull()
        return when {
            valor == null -> "Ingrese un stock entero."
            valor < 0 -> "El stock no puede ser negativo."
            else -> null
        }
    }
}
