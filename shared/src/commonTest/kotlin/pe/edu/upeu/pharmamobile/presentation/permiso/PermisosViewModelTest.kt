package pe.edu.upeu.pharmamobile.presentation.permiso

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import pe.edu.upeu.pharmamobile.domain.permiso.EstadoPermiso
import pe.edu.upeu.pharmamobile.domain.permiso.GestorPermisos
import pe.edu.upeu.pharmamobile.domain.permiso.Permiso
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** La ventaja de la interfaz: se prueba sin dispositivo ni dialogo del sistema. */
private class GestorFalso(
    var actual: EstadoPermiso,
    private val alSolicitar: EstadoPermiso = actual
) : GestorPermisos {
    var solicitudes = 0
    var ajustesAbiertos = 0

    override suspend fun estado(permiso: Permiso) = actual

    override suspend fun solicitar(permiso: Permiso): EstadoPermiso {
        solicitudes++
        actual = alSolicitar
        return actual
    }

    override fun abrirAjustes() {
        ajustesAbiertos++
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class PermisosViewModelTest {

    @BeforeTest
    fun preparar() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun limpiar() = Dispatchers.resetMain()

    private fun viewModelCon(gestor: GestorPermisos) = PermisosViewModel(gestor).also { it.refrescar() }

    @Test
    fun explicaAntesDePedirElPermiso() = runTest {
        val gestor = GestorFalso(EstadoPermiso.Denegado, alSolicitar = EstadoPermiso.Concedido)
        val viewModel = viewModelCon(gestor)

        viewModel.alCambiarAlertas(true)

        assertTrue(viewModel.uiState.value.explicando)
        assertEquals(0, gestor.solicitudes, "el dialogo del sistema aun no aparece")
    }

    @Test
    fun alConcederseLasAlertasQuedanActivas() = runTest {
        val gestor = GestorFalso(EstadoPermiso.Denegado, alSolicitar = EstadoPermiso.Concedido)
        val viewModel = viewModelCon(gestor)
        viewModel.alCambiarAlertas(true)

        viewModel.confirmarExplicacion()

        assertEquals(1, gestor.solicitudes)
        assertTrue(viewModel.uiState.value.alertasActivas)
        assertFalse(viewModel.uiState.value.explicando)
    }

    @Test
    fun siLoNiegaLasAlertasNoSeActivan() = runTest {
        val viewModel = viewModelCon(GestorFalso(EstadoPermiso.Denegado, alSolicitar = EstadoPermiso.Denegado))
        viewModel.alCambiarAlertas(true)

        viewModel.confirmarExplicacion()

        assertFalse(viewModel.uiState.value.alertasActivas)
    }

    @Test
    fun denegadoParaSiempreLlevaAAjustesSinPedir() = runTest {
        val gestor = GestorFalso(EstadoPermiso.DenegadoParaSiempre)
        val viewModel = viewModelCon(gestor)

        viewModel.alCambiarAlertas(true)

        assertEquals(1, gestor.ajustesAbiertos)
        assertEquals(0, gestor.solicitudes)
        assertFalse(viewModel.uiState.value.explicando)
    }

    @Test
    fun cancelarLaExplicacionNoPideElPermiso() = runTest {
        val gestor = GestorFalso(EstadoPermiso.Denegado)
        val viewModel = viewModelCon(gestor)
        viewModel.alCambiarAlertas(true)

        viewModel.cancelarExplicacion()

        assertEquals(0, gestor.solicitudes)
        assertFalse(viewModel.uiState.value.explicando)
    }

    @Test
    fun alVolverDeAjustesSeVeElPermisoNuevo() = runTest {
        val gestor = GestorFalso(EstadoPermiso.DenegadoParaSiempre)
        val viewModel = viewModelCon(gestor)

        gestor.actual = EstadoPermiso.Concedido
        viewModel.refrescar()

        assertTrue(viewModel.uiState.value.alertasActivas)
    }

    @Test
    fun concederDesdeAjustesTrasNegarloActivaLasAlertas() = runTest {
        val gestor = GestorFalso(EstadoPermiso.Denegado, alSolicitar = EstadoPermiso.DenegadoParaSiempre)
        val viewModel = viewModelCon(gestor)
        viewModel.alCambiarAlertas(true)
        viewModel.confirmarExplicacion()
        assertFalse(viewModel.uiState.value.alertasActivas)

        gestor.actual = EstadoPermiso.Concedido
        viewModel.refrescar()

        assertTrue(viewModel.uiState.value.alertasActivas)
    }

    @Test
    fun conPermisoSePuedenApagarLasAlertas() = runTest {
        val viewModel = viewModelCon(GestorFalso(EstadoPermiso.Concedido))

        viewModel.alCambiarAlertas(false)

        assertFalse(viewModel.uiState.value.alertasActivas)
    }
}
