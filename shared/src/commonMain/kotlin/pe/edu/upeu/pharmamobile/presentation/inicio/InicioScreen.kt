package pe.edu.upeu.pharmamobile.presentation.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import pe.edu.upeu.pharmamobile.domain.model.Permisos
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.domain.model.Sesion
import pe.edu.upeu.pharmamobile.navigation.Screen
import pe.edu.upeu.pharmamobile.presentation.components.Capsula
import pe.edu.upeu.pharmamobile.presentation.components.EstadoSincronizacion
import pe.edu.upeu.pharmamobile.presentation.components.IconoCapsula
import pe.edu.upeu.pharmamobile.presentation.components.colorCapsula
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoUiState
import pe.edu.upeu.pharmamobile.presentation.theme.AmbarTexto2
import pe.edu.upeu.pharmamobile.presentation.theme.CoralTinte
import pe.edu.upeu.pharmamobile.presentation.theme.RojoTexto
import pe.edu.upeu.pharmamobile.presentation.theme.fuenteMono

@Composable
fun InicioScreen(
    sesion: Sesion,
    estado: ProductoUiState,
    onNavigate: (Screen) -> Unit,
    onAbrirAjustes: () -> Unit,
    onSincronizar: () -> Unit
) {
    val colores = MaterialTheme.colorScheme
    val ahora = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) }
    val fecha = remember(ahora) { formatearFecha(ahora.dayOfWeek.ordinal, ahora.day, ahora.month.number) }
    val productos = (estado.fase as? ProductoUiState.FaseInventario.ConProductos)?.productos.orEmpty()
    val activos = productos.count { !it.estaInactivo() }
    val porReponer = productos.filter { it.requiereReposicion() }.sortedBy { it.stock }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(fecha, style = MaterialTheme.typography.bodyMedium, color = colores.onSurfaceVariant)
                Text("Hola, ${sesion.nombreCompleto.substringBefore(" ")}", style = MaterialTheme.typography.headlineMedium, color = colores.onSurface)
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colores.surfaceContainerHighest)
                    .clickable(role = Role.Button, onClick = onAbrirAjustes)
                    .semantics { contentDescription = "Cuenta y ajustes" },
                contentAlignment = Alignment.Center
            ) {
                Text(iniciales(sesion.nombreCompleto), style = MaterialTheme.typography.titleSmall, color = colores.onSurface)
            }
        }

        EstadoSincronizacion(
            sincronizando = estado.sincronizando,
            aviso = estado.avisoRed,
            pendientes = estado.pendientes,
            ultimaSincronizacion = estado.ultimaSincronizacion,
            onSincronizar = onSincronizar
        )

        if (sesion.vencida) {
            // Escenario 5: token caducado y sin red. Se consulta lo guardado; al reconectar,
            // la primera peticion recibe 401 y la app pide iniciar sesion de nuevo.
            Text(
                "Tu sesión venció. Puedes consultar los datos guardados; al recuperar la conexión te pediremos iniciar sesión otra vez.",
                style = MaterialTheme.typography.bodySmall,
                color = AmbarTexto2
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Indicador("Activos", "$activos", Modifier.weight(1f))
            Indicador("Por reponer", "${porReponer.size}", Modifier.weight(1f), fondo = CoralTinte, colorTexto = RojoTexto)
            Indicador("Por enviar", "${estado.pendientes}", Modifier.weight(1f))
        }

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Requiere atención", style = MaterialTheme.typography.titleLarge, color = colores.onSurface, modifier = Modifier.weight(1f))
                TextButton(onClick = { onNavigate(Screen.Productos) }) {
                    Text("Ver todo", style = MaterialTheme.typography.labelLarge, color = colores.onSurfaceVariant)
                }
            }
            if (porReponer.isEmpty()) {
                Text("Todo el stock está sobre el mínimo.", style = MaterialTheme.typography.bodyMedium, color = colores.onSurfaceVariant)
            } else {
                porReponer.take(4).forEachIndexed { indice, producto ->
                    FilaReposicion(producto, onClick = { onNavigate(Screen.Productos) })
                    if (indice < minOf(porReponer.size, 4) - 1) HorizontalDivider(color = colores.outlineVariant)
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Accesos", style = MaterialTheme.typography.titleLarge, color = colores.onSurface)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (sesion.puede(Permisos.CATALOGO_LEER)) {
                    Acceso("Inventario", Modifier.weight(1f), onClick = { onNavigate(Screen.Productos) }) { IconoCapsula(it) }
                }
                if (sesion.puede(Permisos.VENTAS_LEER)) {
                    Acceso("Pedidos", Modifier.weight(1f), onClick = { onNavigate(Screen.Pedidos) }) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = it)
                    }
                }
                if (sesion.puede(Permisos.CLIENTES_LEER)) {
                    Acceso("Clientes", Modifier.weight(1f), onClick = { onNavigate(Screen.Clientes) }) {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = it)
                    }
                }
            }
        }
    }
}

@Composable
private fun Indicador(
    etiqueta: String,
    valor: String,
    modifier: Modifier = Modifier,
    fondo: Color = MaterialTheme.colorScheme.surfaceVariant,
    colorTexto: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(16.dp)).background(fondo).padding(horizontal = 12.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(etiqueta, style = MaterialTheme.typography.bodySmall, color = if (fondo == CoralTinte) colorTexto else MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor, style = MaterialTheme.typography.headlineSmall.copy(fontFamily = fuenteMono()), color = colorTexto)
    }
}

@Composable
private fun FilaReposicion(producto: Producto, onClick: () -> Unit) {
    val colores = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Capsula(color = colorCapsula(producto))
        Column(modifier = Modifier.weight(1f)) {
            Text(producto.nombre, style = MaterialTheme.typography.titleMedium, color = colores.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("Mínimo ${Producto.STOCK_MINIMO} u.", style = MaterialTheme.typography.bodySmall, color = colores.onSurfaceVariant)
        }
        Text("${producto.stock} u.", style = MaterialTheme.typography.titleMedium.copy(fontFamily = fuenteMono()), color = RojoTexto)
    }
}

@Composable
private fun Acceso(titulo: String, modifier: Modifier, onClick: () -> Unit, icono: @Composable (Color) -> Unit) {
    val colores = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .height(88.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, colores.outlineVariant, RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        icono(colores.onSurface)
        Spacer(Modifier.height(4.dp))
        Text(titulo, style = MaterialTheme.typography.labelLarge, color = colores.onSurface)
    }
}

private fun iniciales(nombre: String): String =
    nombre.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }

private val DIAS = listOf("lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo")
private val MESES = listOf(
    "enero", "febrero", "marzo", "abril", "mayo", "junio",
    "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
)

private fun formatearFecha(diaSemanaOrdinal: Int, diaMes: Int, mes: Int): String {
    val dia = DIAS[diaSemanaOrdinal].replaceFirstChar { it.uppercase() }
    return "$dia, $diaMes de ${MESES[mes - 1]}"
}
