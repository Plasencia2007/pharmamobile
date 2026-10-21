package pe.edu.upeu.pharmamobile.domain.usecase

import kotlinx.coroutines.flow.Flow
import pe.edu.upeu.pharmamobile.data.remote.ejecutarLlamada
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository

/** La pantalla se suscribe una vez a la base local y se actualiza sola. */
class ObservarProductosUseCase(
    private val repository: ProductoRepository
) {
    operator fun invoke(): Flow<List<Producto>> = repository.observar()
}

/** Sube lo pendiente y despues trae la lista del servidor; un fallo de red no toca los datos locales. */
class SincronizarProductosUseCase(
    private val repository: ProductoRepository
) {
    suspend operator fun invoke(): Result<Unit> =
        ejecutarLlamada { repository.sincronizar() }
}

/** Instante de la ultima sincronizacion correcta, para decirle al usuario que tan viejos son sus datos. */
class ObservarUltimaSincronizacionUseCase(
    private val repository: ProductoRepository
) {
    operator fun invoke(): Flow<Long?> = repository.observarUltimaSincronizacion()
}
