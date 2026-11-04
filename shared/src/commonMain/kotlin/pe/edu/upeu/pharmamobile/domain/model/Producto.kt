package pe.edu.upeu.pharmamobile.domain.model

data class Producto(
    val id: Long,
    val nombre: String,
    val precio: Double,
    val stock: Int,
    val activo: Boolean = true,
    /** Creado o modificado en el dispositivo y todavia desconocido para el servidor (offline-first). */
    val pendiente: Boolean = false,
    /** Veces que se intento enviar esta fila pendiente al servidor. */
    val intentos: Int = 0,
    /** Por que fallo el ultimo envio; null si todavia no fallo. */
    val ultimoError: String? = null
) {
    companion object {
        const val STOCK_MINIMO = 5

        /** Tope de la cola de envio: al alcanzarlo la fila deja de reintentarse y se muestra como fallida. */
        const val MAX_INTENTOS_ENVIO = 5
    }

    /** Fila pendiente que agoto sus intentos o fue rechazada: espera una decision del usuario. */
    fun envioFallido(): Boolean = pendiente && intentos >= MAX_INTENTOS_ENVIO

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

    // Regla de negocio propia: un producto sin unidades se considera inactivo
    // sin importar la bandera "activo", porque deja de estar disponible para la venta.
    fun estaInactivo(): Boolean = !activo || stock == 0

    // Regla de negocio propia: requiere reposición si está activo y su stock
    // llegó al umbral mínimo configurado (STOCK_MINIMO), pero aún no está en 0.
    fun requiereReposicion(): Boolean = !estaInactivo() && stock <= STOCK_MINIMO
}
