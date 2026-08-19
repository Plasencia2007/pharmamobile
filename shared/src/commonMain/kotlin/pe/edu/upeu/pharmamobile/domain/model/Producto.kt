package pe.edu.upeu.pharmamobile.domain.model

data class Producto(
    val id: Long,
    val nombre: String,
    val precio: Double,
    val stock: Int
) {
    init {
        require(value = stock > 0) {
            "El stock debe ser mayor que 0"
        }
    }
}
