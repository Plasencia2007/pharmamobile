package pe.edu.upeu.pharmamobile.presentation.inicio

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import pe.edu.upeu.pharmamobile.navigation.Screen
import pe.edu.upeu.pharmamobile.presentation.components.dashedBorder
import pharmamobile.shared.generated.resources.Res
import pharmamobile.shared.generated.resources.pharmamobile_logo

@Composable
fun InicioScreen(onNavigate: (Screen) -> Unit) {
    val ahora = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) }
    val saludo = remember(ahora) { saludoSegunHora(ahora.hour) }
    val fecha = remember(ahora) { formatearFecha(ahora.dayOfWeek.ordinal, ahora.dayOfMonth, ahora.monthNumber) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            Image(
                painter = painterResource(Res.drawable.pharmamobile_logo),
                contentDescription = "Logo PharmaMobile",
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = fecha.uppercase(),
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = buildAnnotatedString {
                    append("$saludo,\n")
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                        append("Ana")
                    }
                },
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 6.dp)
            )
            Text(
                text = "Tu inventario, clientes y pedidos en un mismo lugar.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Text(
            text = "ACCESOS DIRECTOS",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
        )

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Tile(
                    titulo = "Productos",
                    descripcion = "Registro e inventario",
                    icono = Icons.Filled.ShoppingCart,
                    variante = TileVariante.PRIMARIO,
                    onClick = { onNavigate(Screen.Productos) },
                    modifier = Modifier.weight(1f)
                )
                Tile(
                    titulo = "Clientes",
                    descripcion = "Administra tu cartera",
                    icono = Icons.Filled.Person,
                    variante = TileVariante.NORMAL,
                    onClick = { onNavigate(Screen.Clientes) },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Tile(
                    titulo = "Pedidos",
                    descripcion = "Controla despachos",
                    icono = Icons.AutoMirrored.Filled.List,
                    variante = TileVariante.NORMAL,
                    onClick = { onNavigate(Screen.Pedidos) },
                    modifier = Modifier.weight(1f)
                )
                Tile(
                    titulo = "Nuevo pedido",
                    descripcion = "Empieza en 30 s",
                    icono = Icons.Filled.Add,
                    variante = TileVariante.FANTASMA,
                    onClick = { onNavigate(Screen.Pedidos) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

private enum class TileVariante { PRIMARIO, NORMAL, FANTASMA }

@Composable
private fun Tile(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    variante: TileVariante,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colores = MaterialTheme.colorScheme
    val fondo = when (variante) {
        TileVariante.PRIMARIO -> colores.primary
        TileVariante.NORMAL -> colores.secondaryContainer
        TileVariante.FANTASMA -> colores.background
    }
    val colorTexto = if (variante == TileVariante.PRIMARIO) colores.onPrimary else colores.onSurface
    val colorDescripcion = if (variante == TileVariante.PRIMARIO) {
        colores.onPrimary.copy(alpha = 0.7f)
    } else {
        colores.onSurfaceVariant
    }
    val colorIcono = if (variante == TileVariante.PRIMARIO) colores.onPrimary else colores.primary

    var base = modifier
        .height(128.dp)
        .clip(RoundedCornerShape(20.dp))
        .background(fondo)
    base = if (variante == TileVariante.FANTASMA) {
        base.dashedBorder(color = colores.outline, cornerRadius = 20.dp)
    } else {
        base
    }

    Column(
        modifier = base
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        if (variante == TileVariante.FANTASMA) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(colores.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, contentDescription = null, tint = colores.primary, modifier = Modifier.size(18.dp))
            }
        } else {
            Icon(icono, contentDescription = null, tint = colorIcono, modifier = Modifier.size(24.dp))
        }
        Column {
            Text(titulo, style = MaterialTheme.typography.titleMedium, color = colorTexto)
            Text(
                descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = colorDescripcion,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

private fun saludoSegunHora(hora: Int): String = when (hora) {
    in 5..11 -> "Buenos días"
    in 12..18 -> "Buenas tardes"
    else -> "Buenas noches"
}

private val DIAS = listOf("lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo")
private val MESES = listOf(
    "enero", "febrero", "marzo", "abril", "mayo", "junio",
    "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
)

private fun formatearFecha(diaSemanaOrdinal: Int, diaMes: Int, mes: Int): String {
    val dia = DIAS[diaSemanaOrdinal].replaceFirstChar { it.uppercase() }
    return "$dia, $diaMes de ${MESES[mes - 1]}"
}
