package pe.edu.upeu.pharmamobile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import pe.edu.upeu.pharmamobile.presentation.components.PharmaTopBar
import pe.edu.upeu.pharmamobile.presentation.config.ConfigScreen
import pe.edu.upeu.pharmamobile.presentation.inicio.InicioScreen
import pe.edu.upeu.pharmamobile.presentation.pedido.PedidoScreen
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoScreen
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoViewModel
import pe.edu.upeu.pharmamobile.presentation.sesion.LoginScreen
import pe.edu.upeu.pharmamobile.presentation.sesion.SesionViewModel
import pe.edu.upeu.pharmamobile.presentation.theme.PharmaMobileTheme

private val destinos = listOf(Screen.Inicio, Screen.Productos, Screen.Clientes, Screen.Pedidos, Screen.Config)

@OptIn(ExperimentalMaterial3Api::class)
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

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val mostrarExito: (String) -> Unit = { mensaje ->
        scope.launch { snackbarHostState.showSnackbar(mensaje) }
    }

    PharmaMobileTheme(darkTheme = darkTheme) {
        if (sesion == null) {
            if (sesionUi.restaurando) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
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
            return@PharmaMobileTheme
        }
        val destinosVisibles = destinos.filter { puedeVer(it, sesion) }
        Scaffold(
            topBar = { PharmaTopBar(seccionActual = pantallaActual.titulo) },
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    Snackbar(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = RoundedCornerShape(16.dp),
                        content = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                                Text(
                                    text = data.visuals.message,
                                    modifier = Modifier.padding(start = 10.dp),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    )
                }
            },
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
                    destinosVisibles.forEach { destino ->
                        NavigationBarItem(
                            selected = pantallaActual == destino,
                            onClick = { pantallaActual = destino },
                            icon = { Icon(iconoPara(destino), contentDescription = null) },
                            label = { Text(destino.titulo) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                when (pantallaActual) {
                    is Screen.Inicio -> InicioScreen(sesion = sesion, onNavigate = { pantallaActual = it })
                    is Screen.Productos -> {
                        val productoViewModel = koinViewModel<ProductoViewModel>(key = "productos-${sesionUi.generacion}")
                        ProductoScreen(
                            viewModel = productoViewModel,
                            onSuccess = mostrarExito,
                            puedeEscribir = sesion.puede(Permisos.CATALOGO_ESCRIBIR)
                        )
                    }
                    is Screen.Clientes -> ClienteScreen(onSuccess = mostrarExito)
                    is Screen.Pedidos -> PedidoScreen(onSuccess = mostrarExito)
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
                        alAlterarToken = sesionViewModel::alterarTokenGuardado
                    )
                }
            }
        }
    }
}

/** Cada sección se ofrece solo si el rol tiene el permiso que el servidor exige; si no, respondería 403. */
private fun puedeVer(screen: Screen, sesion: Sesion) = when (screen) {
    is Screen.Productos -> sesion.puede(Permisos.CATALOGO_LEER)
    is Screen.Clientes -> sesion.puede(Permisos.CLIENTES_LEER)
    is Screen.Pedidos -> sesion.puede(Permisos.VENTAS_LEER)
    else -> true
}

private fun iconoPara(screen: Screen) = when (screen) {
    is Screen.Inicio -> Icons.Filled.Home
    is Screen.Productos -> Icons.Filled.ShoppingCart
    is Screen.Clientes -> Icons.Filled.Person
    is Screen.Pedidos -> Icons.AutoMirrored.Filled.List
    is Screen.Config -> Icons.Filled.Settings
}
