package pe.edu.upeu.pharmamobile.presentation.producto

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobile.domain.usecase.RegistrarProductoUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Repositorio falso que implementa el CONTRATO de dominio (ProductoRepository),
 * no la clase en memoria — así la prueba compila contra la interfaz, tal como
 * pide la guía autónoma.
 */
private class FakeProductoRepository(
    productosIniciales: List<Producto> = emptyList(),
    private val fallarAlListar: Boolean = false
) : ProductoRepository {

    private val productos = productosIniciales.toMutableList()

    var registrarLlamadas = 0
        private set

    override suspend fun registrar(producto: Producto): Producto {
        registrarLlamadas++
        val conId = producto.copy(id = (productos.maxOfOrNull { it.id } ?: 0L) + 1)
        productos.add(conId)
        return conId
    }

    override suspend fun listar(): List<Producto> {
        if (fallarAlListar) error("Fallo simulado del repositorio")
        return productos.toList()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ProductoViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun repositorioVacioProduceSinProductos() = runTest {
        val fake = FakeProductoRepository(productosIniciales = emptyList())
        val viewModel = ProductoViewModel(RegistrarProductoUseCase(fake), fake)

        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(ProductoUiState.FaseInventario.SinProductos, viewModel.uiState.value.fase)
    }

    @Test
    fun repositorioConProductosProduceConProductos() = runTest {
        val productos = listOf(
            Producto(id = 1L, nombre = "Paracetamol", precio = 15.50, stock = 100),
            Producto(id = 2L, nombre = "Ibuprofeno", precio = 18.90, stock = 50),
            Producto(id = 3L, nombre = "Amoxicilina", precio = 25.00, stock = 5)
        )
        val fake = FakeProductoRepository(productosIniciales = productos)
        val viewModel = ProductoViewModel(RegistrarProductoUseCase(fake), fake)

        dispatcher.scheduler.advanceUntilIdle()

        val fase = viewModel.uiState.value.fase
        assertTrue(fase is ProductoUiState.FaseInventario.ConProductos)
        assertEquals(3, fase.productos.size)
    }

    @Test
    fun repositorioQueLanzaExcepcionProduceError() = runTest {
        val fake = FakeProductoRepository(fallarAlListar = true)
        val viewModel = ProductoViewModel(RegistrarProductoUseCase(fake), fake)

        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.fase is ProductoUiState.FaseInventario.Error)
    }

    @Test
    fun registroConPrecioCeroDejaErrorEnFormularioSinLlamarAlRepositorio() = runTest {
        val fake = FakeProductoRepository(productosIniciales = emptyList())
        val viewModel = ProductoViewModel(RegistrarProductoUseCase(fake), fake)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.registrarProducto(nombre = "Aspirina", precio = "0", stock = "5")
        dispatcher.scheduler.advanceUntilIdle()

        val formulario = viewModel.uiState.value.formulario
        assertNotNull(formulario.errorPrecio)
        assertEquals(0, fake.registrarLlamadas)
    }
}
