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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import pe.edu.upeu.pharmamobile.presentation.theme.Amber
import pe.edu.upeu.pharmamobile.presentation.theme.AmberSoft
import pe.edu.upeu.pharmamobile.presentation.theme.Coral
import pe.edu.upeu.pharmamobile.presentation.theme.CoralSoft
import pe.edu.upeu.pharmamobile.presentation.theme.Emerald
import pe.edu.upeu.pharmamobile.presentation.theme.EmeraldDeep
import pe.edu.upeu.pharmamobile.presentation.theme.Violet
import pe.edu.upeu.pharmamobile.presentation.theme.VioletSoft
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(EmeraldDeep, Emerald),
                        start = Offset(0f, 0f),
                        end = Offset(400f, 400f)
                    )
                )
                .padding(22.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(Res.drawable.pharmamobile_logo),
                        contentDescription = "Logo PharmaMobile",
                        modifier = Modifier.size(34.dp)
                    )
                    Text(
                        text = fecha.uppercase(),
                        modifier = Modifier.padding(start = 10.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
                Text(
                    text = buildAnnotatedString {
                        append("$saludo,\n")
                        withStyle(SpanStyle(color = AmberSoft)) {
                            append("Ana")
                        }
                    },
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    modifier = Modifier.padding(top = 14.dp)
                )
                Text(
                    text = "Tu inventario, clientes y pedidos en un mismo lugar.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.82f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Text(
            text = "Accesos directos",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
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
                    acento = Emerald,
                    fondoSuave = MaterialTheme.colorScheme.primaryContainer,
                    onClick = { onNavigate(Screen.Productos) },
                    modifier = Modifier.weight(1f)
                )
                Tile(
                    titulo = "Clientes",
                    descripcion = "Administra tu cartera",
                    icono = Icons.Filled.Person,
                    acento = Violet,
                    fondoSuave = VioletSoft,
                    onClick = { onNavigate(Screen.Clientes) },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Tile(
                    titulo = "Pedidos",
                    descripcion = "Controla despachos",
                    icono = Icons.AutoMirrored.Filled.List,
                    acento = Amber,
                    fondoSuave = AmberSoft,
                    onClick = { onNavigate(Screen.Pedidos) },
                    modifier = Modifier.weight(1f)
                )
                Tile(
                    titulo = "Nuevo pedido",
                    descripcion = "Empieza en 30 s",
                    icono = Icons.Filled.Add,
                    acento = Coral,
                    fondoSuave = CoralSoft,
                    onClick = { onNavigate(Screen.Pedidos) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun Tile(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    acento: Color,
    fondoSuave: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colores = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .height(132.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(colores.surface)
            .background(fondoSuave.copy(alpha = 0.55f))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(acento),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, contentDescription = null, tint = Color.White, modifier = Modifier.size(19.dp))
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = acento.copy(alpha = 0.55f),
                modifier = Modifier.size(16.dp)
            )
        }
        Column {
            Text(titulo, style = MaterialTheme.typography.titleMedium, color = colores.onSurface)
            Text(
                descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = colores.onSurfaceVariant,
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
