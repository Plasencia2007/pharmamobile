package pe.edu.upeu.pharmamobile.presentation.pedido

import pe.edu.upeu.pharmamobile.presentation.cliente.ClienteValidator

object PedidoValidator {

    fun validarNombreCliente(nombre: String): String? = ClienteValidator.validarNombre(nombre)

    fun validarCorreoCliente(correo: String): String? = ClienteValidator.validarCorreo(correo)

    fun validarProducto(nombre: String): String? {
        return if (nombre.isBlank()) "El nombre del producto es obligatorio." else null
    }

    fun validarPrecio(precio: String): String? {
        val valor = precio.toDoubleOrNull()
        return when {
            valor == null -> "Ingrese un precio numérico."
            valor <= 0 -> "El precio debe ser mayor que cero."
            else -> null
        }
    }

    fun validarCantidad(cantidad: String): String? {
        val valor = cantidad.toIntOrNull()
        return when {
            valor == null -> "Ingrese una cantidad entera."
            valor <= 0 -> "La cantidad debe ser mayor que cero."
            else -> null
        }
    }
}
