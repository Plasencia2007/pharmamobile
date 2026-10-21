package pe.edu.upeu.pharmamobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.launch
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.qualifier.named
import pe.edu.upeu.pharmamobile.di.NOMBRE_DEPURACION
import pe.edu.upeu.pharmamobile.domain.model.Permisos
import pe.edu.upeu.pharmamobile.domain.model.Sesion
import pe.edu.upeu.pharmamobile.navigation.Screen
import pe.edu.upeu.pharmamobile.presentation.cliente.ClienteScreen
import pe.edu.upeu.pharmamobile.presentation.components.DockNavegacion
import pe.edu.upeu.pharmamobile.presentation.components.IconoCapsula
import pe.edu.upeu.pharmamobile.presentation.components.ItemDock
import pe.edu.upeu.pharmamobile.presentation.config.ConfigScreen
import pe.edu.upeu.pharmamobile.presentation.inicio.InicioScreen
import pe.edu.upeu.pharmamobile.presentation.pedido.PedidoScreen
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoScreen
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoUiState
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoViewModel
import pe.edu.upeu.pharmamobile.presentation.sesion.LoginScreen
import pe.edu.upeu.pharmamobile.presentation.sesion.SesionViewModel
import pe.edu.upeu.pharmamobile.presentation.theme.PharmaMobileTheme

/** Secciones del dock, en orden. Ajustes no está: se abre desde el avatar de Inicio. */
private val destinosDock = listOf(Screen.Inicio, Screen.Productos, Screen.Pedidos, Screen.Clientes)

@Composable
@Preview
fun App() = KoinContext {
    val sesionViewModel = koinViewModel<SesionViewModel>()
    val depuracion: Boolean = koinInject(named(NOMBRE_DEPURACION))
    val sesionUi by sesionViewModel.uiState.collectAsStateWithLifecycle()
    val sesion = sesionUi.sesion
    var darkTheme by remember { mutableStateOf(false) }
    var alertasStockBajo by remember { mutableStateOf(true) }
    // Al cambiar de usuario se vuelve a Inicio: el nuevo rol puede no tener acceso a la sección anterior.
    var pantallaActual by remember(sesion?.usuario) { mutableStateOf<Screen>(Screen.Inicio) }
    // Cada toque del "+" del dock incrementa este contador; Inventario abre el formulario al verlo cambiar.
    var solicitudNuevo by remember(sesion?.usuario) { mutableIntStateOf(0) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val mostrarExito: (String) -> Unit = { mensaje ->
        scope.launch { snackbarHostState.showSnackbar(mensaje) }
    }

    PharmaMobileTheme(darkTheme = darkTheme) {
        val fondo = MaterialTheme.colorScheme.background
        if (sesion == null) {
            Box(modifier = Modifier.fillMaxSize().background(fondo).statusBarsPadding()) {
                if (sesionUi.restaurando) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.align(Alignment.Center))
                } else {
                    LoginScreen(
                        estado = sesionUi,
                        alCambiarUsuario = sesionViewModel::alCambiarUsuario,
                        alCambiarClave = sesionViewModel::alCambiarClave,
                        alIniciar = sesionViewModel::iniciarSesion,
                        depuracion = depuracion,
                        alProbarSinToken = { sesionViewModel.probarAcceso("productos") }
                    )
                }
            }
            return@PharmaMobileTheme
        }

        // Inicio e Inventario comparten el mismo ViewModel: los indicadores de Inicio
        // salen de la misma base local que la lista. Sin permiso de catálogo no se crea.
        val puedeCatalogo = sesion.puede(Permisos.CATALOGO_LEER)
        val productoViewModel = if (puedeCatalogo) {
            koinViewModel<ProductoViewModel>(key = "productos-${sesionUi.generacion}")
        } else null
        val estadoProductos = productoViewModel?.uiState?.collectAsStateWithLifecycle()?.value ?: ProductoUiState()
        val puedeEscribir = sesion.puede(Permisos.CATALOGO_ESCRIBIR)

        // Atrás del sistema: desde cualquier sección (Ajustes incluido) se vuelve a Inicio;
        // en Inicio no se intercepta y el sistema cierra la app como de costumbre.
        NavigationBackHandler(
            state = rememberNavigationEventState(NavigationEventInfo.None),
            isBackEnabled = pantallaActual != Screen.Inicio,
            onBackCompleted = { pantallaActual = Screen.Inicio }
        )

        Box(modifier = Modifier.fillMaxSize().background(fondo).statusBarsPadding()) {
            when (pantallaActual) {
                is Screen.Inicio -> InicioScreen(
                    sesion = sesion,
                    estado = estadoProductos,
                    onNavigate = { pantallaActual = it },
                    onAbrirAjustes = { pantallaActual = Screen.Config },
                    onSincronizar = { productoViewModel?.sincronizar() }
                )
                is Screen.Productos -> if (productoViewModel != null) {
                    ProductoScreen(
                        viewModel = productoViewModel,
                        onSuccess = mostrarExito,
                        puedeEscribir = puedeEscribir,
                        solicitudNuevo = solicitudNuevo
                    )
                }
                is Screen.Clientes -> ConMargenDock { ClienteScreen(onSuccess = mostrarExito) }
                is Screen.Pedidos -> ConMargenDock { PedidoScreen(onSuccess = mostrarExito) }
                is Screen.Config -> ConfigScreen(
                    darkTheme = darkTheme,
                    onDarkThemeChange = { darkTheme = it },
                    alertasStockBajo = alertasStockBajo,
                    onAlertasStockBajoChange = { alertasStockBajo = it },
                    sesion = sesion,
                    diagnostico = sesionUi.diagnostico,
                    alProbarClientes = { sesionViewModel.probarAcceso("clientes") },
                    alCerrarSesion = sesionViewModel::cerrarSesion,
                    depuracion = depuracion,
                    alAlterarToken = sesionViewModel::alterarTokenGuardado,
                    onVolver = { pantallaActual = Screen.Inicio },
                    guardados = (estadoProductos.fase as? ProductoUiState.FaseInventario.ConProductos)?.productos?.size ?: 0,
                    porEnviar = estadoProductos.pendientes
                )
            }

            Column(modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding()) {
                SnackbarHost(snackbarHostState, modifier = Modifier.padding(horizontal = 16.dp)) { data ->
                    Snackbar(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                            Text(
                                text = data.visuals.message,
                                modifier = Modifier.padding(start = 10.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
                // En Ajustes el dock se oculta: se vuelve con la flecha.
                if (pantallaActual != Screen.Config) {
                    DockNavegacion(
                        items = destinosDock.filter { puedeVer(it, sesion) }.map(::itemDock),
                        seleccionado = pantallaActual.titulo,
                        onSeleccionar = { id -> destinosDock.firstOrNull { it.titulo == id }?.let { pantallaActual = it } },
                        onNuevo = if (puedeEscribir && puedeCatalogo) {
                            {
                                pantallaActual = Screen.Productos
                                solicitudNuevo++
                            }
                        } else null
                    )
                }
            }
        }
    }
}

/** Clientes y Pedidos se desplazan solos; este margen evita que el dock tape su último campo. */
@Composable
private fun ConMargenDock(contenido: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(bottom = 92.dp)) { contenido() }
}

/** Cada sección se ofrece solo si el rol tiene el permiso que el servidor exige; si no, respondería 403. */
private fun puedeVer(screen: Screen, sesion: Sesion) = when (screen) {
    is Screen.Productos -> sesion.puede(Permisos.CATALOGO_LEER)
    is Screen.Clientes -> sesion.puede(Permisos.CLIENTES_LEER)
    is Screen.Pedidos -> sesion.puede(Permisos.VENTAS_LEER)
    else -> true
}

private fun itemDock(screen: Screen) = ItemDock(
    id = screen.titulo,
    titulo = if (screen is Screen.Productos) "Inventario" else screen.titulo
) { color ->
    when (screen) {
        is Screen.Productos -> IconoCapsula(color)
        is Screen.Pedidos -> Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = color)
        is Screen.Clientes -> Icon(Icons.Filled.Person, contentDescription = null, tint = color)
        else -> Icon(Icons.Filled.Home, contentDescription = null, tint = color)
    }
}
