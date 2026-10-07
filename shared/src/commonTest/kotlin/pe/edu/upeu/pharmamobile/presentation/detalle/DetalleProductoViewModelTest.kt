package pe.edu.upeu.pharmamobile.presentation.detalle

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.platform.Compartidor
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobile.domain.usecase.ObtenerProductoUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class CompartidorFalso : Compartidor {
    val textosCompartidos = mutableListOf<String>()
    override fun compartir(texto: String) {
        textosCompartidos.add(texto)
    }
}

private class RepositorioConUnProducto(private val producto: Producto) : ProductoRepository {
    override suspend fun listar() = listOf(producto)
    override suspend fun obtener(id: Long) = producto
    override suspend fun registrar(producto: Producto) = producto
    override suspend fun actualizar(producto: Producto) = producto
    override suspend fun eliminar(id: Long) = Unit
}

@OptIn(ExperimentalCoroutinesApi::class)
class DetalleProductoViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val paracetamol = Producto(id = 1L, nombre = "Paracetamol 500mg", precio = 15.5, stock = 100)

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun cargaElProductoYPasaAContenido() = runTest {
        val viewModel = DetalleProductoViewModel(
            1L, ObtenerProductoUseCase(RepositorioConUnProducto(paracetamol)), CompartidorFalso()
        )
        dispatcher.scheduler.advanceUntilIdle()

        val estado = viewModel.uiState
        assertTrue(estado is DetalleProductoUiState.Contenido)
        assertEquals("Paracetamol 500mg", estado.ui.nombre)
    }

    @Test
    fun compartirEntregaAlCompartidorElTextoArmadoEnCodigoComun() = runTest {
        val compartidor = CompartidorFalso()
        val viewModel = DetalleProductoViewModel(
            1L, ObtenerProductoUseCase(RepositorioConUnProducto(paracetamol)), compartidor
        )
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.compartir()

        assertEquals(1, compartidor.textosCompartidos.size)
        val texto = compartidor.textosCompartidos.single()
        assertTrue(texto.startsWith("Paracetamol 500mg"), texto)
        assertTrue(texto.endsWith("Stock: 100"), texto)
    }

    @Test
    fun compartirAntesDeCargarNoHaceNada() = runTest {
        val compartidor = CompartidorFalso()
        val viewModel = DetalleProductoViewModel(
            1L, ObtenerProductoUseCase(RepositorioConUnProducto(paracetamol)), compartidor
        )

        viewModel.compartir()

        assertTrue(compartidor.textosCompartidos.isEmpty())
    }
}
