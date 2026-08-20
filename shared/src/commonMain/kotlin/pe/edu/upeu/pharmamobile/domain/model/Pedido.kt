package pe.edu.upeu.pharmamobile.domain.model

data class Pedido(
    val id: Long,
    val cliente: Cliente,
    val detalles: List<DetallePedido>,
    val estado: EstadoPedido
) {
    init {
        require(value = detalles.isNotEmpty()) {
            "El pedido debe tener al menos un detalle"
        }
    }

    fun total(): Double {
        return detalles.sumOf { it.subtotal() }
    }
}
