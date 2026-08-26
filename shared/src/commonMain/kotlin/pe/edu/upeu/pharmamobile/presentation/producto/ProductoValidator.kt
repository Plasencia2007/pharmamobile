package pe.edu.upeu.pharmamobile.presentation.producto

object ProductoValidator {

    fun validarNombre(nombre: String): String? {
        return if (nombre.isBlank()) "Ingrese nombre del producto" else null
    }

    fun validarPrecio(precio: String): String? {
        val valor = precio.toDoubleOrNull()
        return if (valor == null || valor <= 0) "Ingrese precio válido" else null
    }

    fun validarStock(stock: String): String? {
        val valor = stock.toIntOrNull()
        return if (valor == null || valor < 0) "El stock no puede ser negativo" else null
    }
}
