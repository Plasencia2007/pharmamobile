package pe.edu.upeu.pharmamobile.presentation.producto

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.io.IOException
import pe.edu.upeu.pharmamobile.domain.error.ErrorApi
import pe.edu.upeu.pharmamobile.domain.error.ErrorApiException
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.repository.ProductoRepository
import pe.edu.upeu.pharmamobile.domain.usecase.ActualizarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.EliminarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.ObservarProductosUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.RegistrarProductoUseCase
import pe.edu.upeu.pharmamobile.domain.usecase.SincronizarProductosUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Repositorio falso que implementa el CONTRATO de dominio (ProductoRepository),
 * no la clase en memoria — así la prueba compila contra la interfaz, tal como
 * pide la guía autónoma.
 */
private class FakeProductoRepository(
    productosIniciales: List<Producto> = emptyList(),
    private val fallarAlSincronizar: Boolean = false,
    private val fallarAlEliminar: Boolean = false,
    private val errorValidacionAlRegistrar: Map<String, String>? = null
) : ProductoRepository {

    // Como la base local: una fuente que emite de nuevo con cada escritura.
    private val productos = MutableStateFlow(productosIniciales)

    var registrarLlamadas = 0
        private set
    var actualizarLlamadas = 0
        private set
    var eliminarLlamadas = 0
        private set
    var sincronizarLlamadas = 0
        private set

    override fun observar(): Flow<List<Producto>> = productos

    override suspend fun registrar(producto: Producto): Producto {
        if (errorValidacionAlRegistrar != null) {
            throw ErrorApiException(ErrorApi.Validacion(errorValidacionAlRegistrar))
        }
        registrarLlamadas++
        val conId = producto.copy(id = (productos.value.maxOfOrNull { it.id } ?: 0L) + 1)
        productos.value = productos.value + conId
        return conId
    }

    override suspend fun sincronizar() {
        sincronizarLlamadas++
        if (fallarAlSincronizar) throw IOException("Fallo simulado de red")
    }

    override suspend fun obtener(id: Long): Producto =
        productos.value.find { it.id == id } ?: throw NoSuchElementException("Producto $id no encontrado")

    override suspend fun actualizar(producto: Producto): Producto {
        actualizarLlamadas++
        val indice = productos.value.indexOfFirst { it.id == producto.id }
        require(indice >= 0) { "Producto ${producto.id} no encontrado" }
        productos.value = productos.value.toMutableList().also { it[indice] = producto }
        return producto
    }

    override suspend fun eliminar(id: Long) {
        if (fallarAlEliminar) throw IOException("Fallo simulado al eliminar")
        eliminarLlamadas++
        productos.value = productos.value.filterNot { it.id == id }
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

    private fun viewModelCon(fake: FakeProductoRepository) = ProductoViewModel(
        observarProductosUseCase = ObservarProductosUseCase(fake),
        sincronizarProductosUseCase = SincronizarProductosUseCase(fake),
        registrarProductoUseCase = RegistrarProductoUseCase(fake),
        actualizarProductoUseCase = ActualizarProductoUseCase(fake),
        eliminarProductoUseCase = EliminarProductoUseCase(fake)
    )

    @Test
    fun repositorioVacioProduceSinProductos() = runTest {
        val fake = FakeProductoRepository(productosIniciales = emptyList())
        val viewModel = viewModelCon(fake)

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
        val viewModel = viewModelCon(fake)

        dispatcher.scheduler.advanceUntilIdle()

        val fase = viewModel.uiState.value.fase
        assertTrue(fase is ProductoUiState.FaseInventario.ConProductos)
        assertEquals(3, fase.productos.size)
    }

    @Test
    fun sincronizacionSinRedMuestraAvisoYConservaLosDatosGuardados() = runTest {
        val productos = listOf(Producto(id = 1L, nombre = "Paracetamol", precio = 15.50, stock = 100))
        val fake = FakeProductoRepository(productosIniciales = productos, fallarAlSincronizar = true)
        val viewModel = viewModelCon(fake)

        dispatcher.scheduler.advanceUntilIdle()

        val estado = viewModel.uiState.value
        assertEquals("Sin conexión: mostrando datos guardados", estado.avisoRed)
        assertFalse(estado.sincronizando)
        val fase = estado.fase
        assertTrue(fase is ProductoUiState.FaseInventario.ConProductos)
        assertEquals(1, fase.productos.size)
    }

    @Test
    fun sincronizacionExitosaLimpiaElAviso() = runTest {
        val fake = FakeProductoRepository()
        val viewModel = viewModelCon(fake)

        dispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.avisoRed)
        assertEquals(1, fake.sincronizarLlamadas)
    }

    @Test
    fun registrarActualizaLaListaSinPedirlaDeNuevo() = runTest {
        val fake = FakeProductoRepository()
        val viewModel = viewModelCon(fake)
        dispatcher.scheduler.advanceUntilIdle()
        val sincronizacionesAntes = fake.sincronizarLlamadas

        viewModel.registrarProducto(nombre = "Ibuprofeno", precio = "4.5", stock = "20")
        dispatcher.scheduler.advanceUntilIdle()

        val fase = viewModel.uiState.value.fase
        assertTrue(fase is ProductoUiState.FaseInventario.ConProductos)
        assertEquals("Ibuprofeno", fase.productos.single().nombre)
        // La lista cambio porque el Flow emitio, no porque nadie la recargara.
        assertEquals(sincronizacionesAntes, fake.sincronizarLlamadas)
    }

    @Test
    fun registroConPrecioCeroDejaErrorEnFormularioSinLlamarAlRepositorio() = runTest {
        val fake = FakeProductoRepository(productosIniciales = emptyList())
        val viewModel = viewModelCon(fake)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.registrarProducto(nombre = "Aspirina", precio = "0", stock = "5")
        dispatcher.scheduler.advanceUntilIdle()

        val formulario = viewModel.uiState.value.formulario
        assertNotNull(formulario.errorPrecio)
        assertEquals(0, fake.registrarLlamadas)
    }

    @Test
    fun errorDeValidacionDelServidorDejaMensajesEnFormularioSinCambiarFase() = runTest {
        val fake = FakeProductoRepository(
            productosIniciales = emptyList(),
            errorValidacionAlRegistrar = mapOf(
                "precio" to "El precio debe ser mayor o igual a 0.01"
            )
        )
        val viewModel = viewModelCon(fake)
        dispatcher.scheduler.advanceUntilIdle()
        val faseAntes = viewModel.uiState.value.fase

        viewModel.registrarProducto(nombre = "Vitamina D3", precio = "0.001", stock = "10")
        dispatcher.scheduler.advanceUntilIdle()

        val formulario = viewModel.uiState.value.formulario
        assertEquals("El precio debe ser mayor o igual a 0.01", formulario.errorPrecio)
        assertNull(formulario.errorNombre)
        assertEquals(faseAntes, viewModel.uiState.value.fase)
        assertEquals(ProductoUiState.Operacion.Inactiva, viewModel.uiState.value.operacion)
    }

    @Test
    fun actualizarProductoRecargaElListadoConLosNuevosDatos() = runTest {
        val producto = Producto(id = 1L, nombre = "Paracetamol", precio = 15.50, stock = 100)
        val fake = FakeProductoRepository(productosIniciales = listOf(producto))
        val viewModel = viewModelCon(fake)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.actualizarProducto(producto, nombre = "Paracetamol 1g", precio = "20.0", stock = "80")
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fake.actualizarLlamadas)
        val fase = viewModel.uiState.value.fase
        assertTrue(fase is ProductoUiState.FaseInventario.ConProductos)
        assertEquals("Paracetamol 1g", fase.productos.first().nombre)
        assertEquals("Producto actualizado", viewModel.uiState.value.formulario.mensajeExito)
    }

    @Test
    fun eliminarProductoLoQuitaDelListado() = runTest {
        val producto = Producto(id = 1L, nombre = "Paracetamol", precio = 15.50, stock = 100)
        val fake = FakeProductoRepository(productosIniciales = listOf(producto))
        val viewModel = viewModelCon(fake)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.eliminarProducto(producto.id)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, fake.eliminarLlamadas)
        assertEquals(ProductoUiState.FaseInventario.SinProductos, viewModel.uiState.value.fase)
        assertEquals(ProductoUiState.Operacion.Inactiva, viewModel.uiState.value.operacion)
    }

    @Test
    fun eliminarProductoFallidoDejaOperacionFallidaSinTocarElListado() = runTest {
        val producto = Producto(id = 1L, nombre = "Paracetamol", precio = 15.50, stock = 100)
        val fake = FakeProductoRepository(productosIniciales = listOf(producto), fallarAlEliminar = true)
        val viewModel = viewModelCon(fake)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.eliminarProducto(producto.id)
        dispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.operacion is ProductoUiState.Operacion.Fallida)
        val fase = viewModel.uiState.value.fase
        assertTrue(fase is ProductoUiState.FaseInventario.ConProductos)
        assertFalse(fase.productos.isEmpty())
    }
}
