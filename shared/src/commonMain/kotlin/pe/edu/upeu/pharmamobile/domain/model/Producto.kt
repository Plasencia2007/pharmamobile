package pe.edu.upeu.pharmamobile.domain.model

data class Producto(
    val id: Long,
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val activo: Boolean = true
) {
    init {
        require(value = nombre.isNotBlank()) {
            "El nombre no puede estar vacío"
        }

        require(value = precio > 0) {
            "El precio debe ser mayor que 0"
        }

        require(value = stock >= 0) {
            "El stock no puede ser negativo"
        }
    }

    fun verificarStock(cantidad: Int): Boolean {
        return stock >= cantidad
    }

    fun estadoDisponible(): Boolean {
        return stock > 0
    }

    fun valorInventario(): Double {
        return precio * stock
    }

    fun disminuirStock(cantidad: Int): Producto {
        require(value = cantidad > 0) {
            "La cantidad debe ser mayor cero"
        }
        require(value = verificarStock(cantidad)) {
            "Stock insuficiente"
        }
        return copy(
            stock = stock - cantidad
        )
    }
}
