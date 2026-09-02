package pe.edu.upeu.pharmamobile.presentation.producto

object ProductoValidator {

    fun validarNombre(nombre: String): String? {
        return if (nombre.isBlank()) "El nombre es obligatorio." else null
    }

    fun validarPrecio(precio: String): String? {
        val valor = precio.toDoubleOrNull()
        return when {
            valor == null -> "Ingrese un precio numérico."
            valor <= 0 -> "El precio debe ser mayor que cero."
            else -> null
        }
    }

    fun validarStock(stock: String): String? {
        val valor = stock.toIntOrNull()
        return when {
            valor == null -> "Ingrese un stock entero."
            valor < 0 -> "El stock no puede ser negativo."
            else -> null
        }
    }
}
