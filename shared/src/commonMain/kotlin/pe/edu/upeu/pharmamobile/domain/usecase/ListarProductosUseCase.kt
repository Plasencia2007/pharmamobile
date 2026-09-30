package pe.edu.upeu.pharmamobile.domain.usecase

import pe.edu.upeu.pharmamobile.data.remote.ejecutarLlamada
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

class ListarProductosUseCase(
    private val repository: ProductoRepository
) {
    suspend operator fun invoke(): Result<List<Producto>> =
        ejecutarLlamada { repository.listar() }
}
