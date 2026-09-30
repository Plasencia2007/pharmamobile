package pe.edu.upeu.pharmamobile.domain.usecase

import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

class ProductoValidationException(
    val errorNombre: String?,
    val errorPrecio: String?,
    val errorStock: String?
) : Exception("Los datos del producto no son válidos")

class RegistrarProductoUseCase(
    private val repository: ProductoRepository
) {
    suspend operator fun invoke(nombre: String, precio: String, stock: String): Result<Producto> {
        val errorNombre = validarNombre(nombre)
        val errorPrecio = validarPrecio(precio)
        val errorStock = validarStock(stock)

        if (errorNombre != null || errorPrecio != null || errorStock != null) {
            return Result.failure(ProductoValidationException(errorNombre, errorPrecio, errorStock))
        }

        val producto = Producto(
            id = 0L,
            nombre = nombre.trim(),
            precio = precio.toDouble(),
            stock = stock.toInt()
        )
        return Result.success(repository.registrar(producto))
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
