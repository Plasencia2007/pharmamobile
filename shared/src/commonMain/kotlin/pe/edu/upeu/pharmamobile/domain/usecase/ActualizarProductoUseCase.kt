package pe.edu.upeu.pharmamobile.domain.usecase

import pe.edu.upeu.pharmamobile.data.remote.ejecutarLlamada
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

class ActualizarProductoUseCase(
    private val repository: ProductoRepository
) {
    suspend operator fun invoke(
        producto: Producto,
        nombre: String,
        precio: String,
        stock: String
    ): Result<Producto> {
        ValidacionProducto.validar(nombre, precio, stock)?.let { return Result.failure(it) }

        val actualizado = producto.copy(
            nombre = nombre.trim(),
            precio = precio.toDouble(),
            stock = stock.toInt()
        )
        return ejecutarLlamada { repository.actualizar(actualizado) }
    }
}
