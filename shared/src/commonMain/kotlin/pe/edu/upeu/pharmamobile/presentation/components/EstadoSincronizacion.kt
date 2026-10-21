package pe.edu.upeu.pharmamobile.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import pe.edu.upeu.pharmamobile.presentation.producto.describirAntiguedad
import pe.edu.upeu.pharmamobile.presentation.theme.AmbarBorde
import pe.edu.upeu.pharmamobile.presentation.theme.AmbarBordeFuerte
import pe.edu.upeu.pharmamobile.presentation.theme.AmbarTexto
import pe.edu.upeu.pharmamobile.presentation.theme.AmbarTexto2
import pe.edu.upeu.pharmamobile.presentation.theme.AmbarTinte
import pe.edu.upeu.pharmamobile.presentation.theme.Blanco
import pe.edu.upeu.pharmamobile.presentation.theme.Verde
import kotlin.time.Clock

/**
 * Estado de la sincronización con PharmaSoft. Con red es una fila discreta;
 * sin red pasa a un aviso ámbar con "Reintentar". La lista nunca depende de él.
 */
@Composable
fun EstadoSincronizacion(
    sincronizando: Boolean,
    aviso: String?,
    pendientes: Int,
    ultimaSincronizacion: Long?,
    onSincronizar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colores = MaterialTheme.colorScheme
    // "hace 2 minutos" envejece aunque nada cambie: se recalcula cada 30 s.
    val ahora by produceState(Clock.System.now().toEpochMilliseconds(), ultimaSincronizacion) {
        while (true) {
            value = Clock.System.now().toEpochMilliseconds()
            delay(30_000)
        }
    }
    val antiguedad = describirAntiguedad(ultimaSincronizacion, ahora)
    val textoPendientes = when (pendientes) {
        0 -> null
        1 -> "1 cambio se enviará al reconectar."
        else -> "$pendientes cambios se enviarán al reconectar."
    }
    if (aviso != null && !sincronizando) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(AmbarTinte)
                .border(1.dp, AmbarBorde, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = AmbarTexto2, modifier = Modifier.size(20.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Sin conexión con PharmaSoft", style = MaterialTheme.typography.titleSmall, color = AmbarTexto)
                    Text(
                        listOfNotNull(
                            if (ultimaSincronizacion == null) "Todavía no hay datos guardados en este teléfono."
                            else "Muestras los datos guardados.",
                            "$antiguedad.",
                            textoPendientes
                        ).joinToString(" "),
                        style = MaterialTheme.typography.bodySmall,
                        color = AmbarTexto2
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(
                    onClick = onSincronizar,
                    modifier = Modifier.height(40.dp),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AmbarBordeFuerte),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(containerColor = Blanco)
                ) {
                    Text("Reintentar", style = MaterialTheme.typography.labelLarge, color = AmbarTexto)
                }
            }
        }
    } else {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, colores.outlineVariant, RoundedCornerShape(14.dp))
                .padding(start = 14.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (sincronizando) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = colores.primary)
                } else {
                    Box(
                        Modifier.size(8.dp).clip(RoundedCornerShape(4.dp))
                            .background(if (ultimaSincronizacion != null) Verde else colores.outline)
                    )
                }
                Column {
                    Text(
                        when {
                            sincronizando -> "Sincronizando…"
                            ultimaSincronizacion == null -> "Aún sin sincronizar"
                            else -> "Sincronizado"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = colores.onSurface
                    )
                    Text(antiguedad, style = MaterialTheme.typography.bodySmall, color = colores.onSurfaceVariant)
                    if (textoPendientes != null) {
                        Text(textoPendientes, style = MaterialTheme.typography.bodySmall, color = colores.onSurfaceVariant)
                    }
                }
            }
            TextButton(onClick = onSincronizar, enabled = !sincronizando) {
                Text("Sincronizar", style = MaterialTheme.typography.labelLarge, color = colores.onSurface, softWrap = false)
            }
        }
    }
}
