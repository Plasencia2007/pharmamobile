package pe.edu.upeu.pharmamobile.presentation.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.ButtonDefaults
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
import pe.edu.upeu.pharmamobile.presentation.sincronizacion.SincronizacionUiState
import pe.edu.upeu.pharmamobile.presentation.sincronizacion.cambios
import pe.edu.upeu.pharmamobile.presentation.theme.AmbarBorde
import pe.edu.upeu.pharmamobile.presentation.theme.AmbarBordeFuerte
import pe.edu.upeu.pharmamobile.presentation.theme.AmbarTexto
import pe.edu.upeu.pharmamobile.presentation.theme.AmbarTexto2
import pe.edu.upeu.pharmamobile.presentation.theme.AmbarTinte
import pe.edu.upeu.pharmamobile.presentation.theme.Blanco
import pe.edu.upeu.pharmamobile.presentation.theme.CoralTinte
import pe.edu.upeu.pharmamobile.presentation.theme.RojoBorde
import pe.edu.upeu.pharmamobile.presentation.theme.RojoTexto
import pe.edu.upeu.pharmamobile.presentation.theme.Verde
import kotlin.time.Clock

/**
 * Estado de la sincronización con PharmaSoft. Con red es una fila discreta;
 * si algo falló pasa a un aviso con su salida. La lista nunca depende de él.
 */
@Composable
fun EstadoSincronizacion(
    estado: SincronizacionUiState,
    onSincronizar: () -> Unit,
    onReintentarFallidas: () -> Unit,
    onRevisarFallidas: () -> Unit,
    modifier: Modifier = Modifier
) {
    // "hace 2 minutos" envejece aunque nada cambie: se recalcula cada 30 s.
    val ahora by produceState(Clock.System.now().toEpochMilliseconds(), estado.ultimaSync) {
        while (true) {
            value = Clock.System.now().toEpochMilliseconds()
            delay(30_000)
        }
    }
    val antiguedad = describirAntiguedad(estado.ultimaSync, ahora)
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (estado.fallidas > 0) {
            AvisoFallidas(estado.fallidas, estado.enCurso, onReintentarFallidas, onRevisarFallidas)
        }
        if (estado.aviso != null && !estado.enCurso) {
            AvisoSinSincronizar(estado, antiguedad, onSincronizar)
        } else {
            FilaEstado(estado, antiguedad, onSincronizar)
        }
    }
}

/** Filas que el servidor rechazó: el mensaje siempre ofrece una acción. */
@Composable
private fun AvisoFallidas(fallidas: Int, enCurso: Boolean, onReintentar: () -> Unit, onRevisar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CoralTinte)
            .border(1.dp, RojoBorde, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = RojoTexto, modifier = Modifier.size(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "${cambios(fallidas)} sin enviar",
                    style = MaterialTheme.typography.titleSmall,
                    color = RojoTexto
                )
                Text(
                    "PharmaSoft no lo aceptó. Revisa el motivo, corrígelo o descártalo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = RojoTexto
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)) {
            TextButton(onClick = onRevisar) {
                Text("Revisar", style = MaterialTheme.typography.labelLarge, color = RojoTexto, softWrap = false)
            }
            OutlinedButton(
                onClick = onReintentar,
                enabled = !enCurso,
                modifier = Modifier.height(40.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, RojoBorde),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Blanco)
            ) {
                Text("Reintentar", style = MaterialTheme.typography.labelLarge, color = RojoTexto, softWrap = false)
            }
        }
    }
}

@Composable
private fun AvisoSinSincronizar(estado: SincronizacionUiState, antiguedad: String, onSincronizar: () -> Unit) {
    val porEnviar = if (estado.pendientes > 0) "${cambios(estado.pendientes)} por enviar" else null
    Column(
        modifier = Modifier
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
                Text(
                    if (estado.sinConexion) {
                        listOfNotNull("Sin conexión", porEnviar).joinToString(" · ")
                    } else {
                        "No se pudo sincronizar"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = AmbarTexto
                )
                Text(
                    listOfNotNull(
                        when {
                            !estado.sinConexion -> estado.aviso
                            estado.ultimaSync == null -> "Todavía no hay datos guardados en este teléfono."
                            else -> "Muestras los datos guardados."
                        },
                        "$antiguedad."
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
                border = BorderStroke(1.dp, AmbarBordeFuerte),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Blanco)
            ) {
                Text("Reintentar", style = MaterialTheme.typography.labelLarge, color = AmbarTexto, softWrap = false)
            }
        }
    }
}

@Composable
private fun FilaEstado(estado: SincronizacionUiState, antiguedad: String, onSincronizar: () -> Unit) {
    val colores = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
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
            if (estado.enCurso) {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = colores.primary)
            } else {
                Box(
                    Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(
                        if (estado.ultimaSync != null && estado.pendientes == 0) Verde else colores.outline
                    )
                )
            }
            Column {
                Text(
                    when {
                        estado.enCurso -> "Sincronizando…"
                        estado.pendientes > 0 -> "${cambios(estado.pendientes)} por enviar"
                        estado.ultimaSync == null -> "Aún sin sincronizar"
                        else -> "Sincronizado"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = colores.onSurface
                )
                Text(antiguedad, style = MaterialTheme.typography.bodySmall, color = colores.onSurfaceVariant)
            }
        }
        TextButton(onClick = onSincronizar, enabled = !estado.enCurso) {
            Text("Sincronizar", style = MaterialTheme.typography.labelLarge, color = colores.onSurface, softWrap = false)
        }
    }
}
