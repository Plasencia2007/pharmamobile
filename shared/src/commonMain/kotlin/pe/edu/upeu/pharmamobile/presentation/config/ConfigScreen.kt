package pe.edu.upeu.pharmamobile.presentation.config

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobile.domain.model.Sesion
import pe.edu.upeu.pharmamobile.platform.InfoDispositivo
import pe.edu.upeu.pharmamobile.presentation.sesion.SesionUiState

@Composable
fun ConfigScreen(
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    alertasStockBajo: Boolean,
    onAlertasStockBajoChange: (Boolean) -> Unit,
    sesion: Sesion?,
    diagnostico: SesionUiState.Diagnostico,
    alProbarClientes: () -> Unit,
    alCerrarSesion: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text(
            text = "Configuración",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Preferencias de la aplicación.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp, bottom = 22.dp)
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Preferencia(
                titulo = "Tema oscuro",
                descripcion = "Cambia la apariencia de toda la app",
                checked = darkTheme,
                onCheckedChange = onDarkThemeChange
            )
            HorizontalDivider()
            Preferencia(
                titulo = "Alertas de stock bajo",
                descripcion = "Avisar cuando queden menos de 10 uds.",
                checked = alertasStockBajo,
                onCheckedChange = onAlertasStockBajoChange
            )
        }

        Column(modifier = Modifier.padding(top = 22.dp, start = 4.dp)) {
            Text(
                text = "CUENTA",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = sesion?.nombreCompleto ?: "Sin sesión",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = sesion?.let { "@${it.usuario} · ${it.permisos.size} permisos" } ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        DiagnosticoPermisos(
            diagnostico = diagnostico,
            alProbar = alProbarClientes,
            modifier = Modifier.padding(top = 22.dp)
        )

        AcercaDe(modifier = Modifier.padding(top = 22.dp))

        OutlinedButton(
            onClick = alCerrarSesion,
            modifier = Modifier.padding(top = 22.dp, bottom = 8.dp).fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Cerrar sesión", color = MaterialTheme.colorScheme.error)
        }
    }
}

/** Pide un recurso protegido para observar qué responde el servidor según el rol. */
@Composable
private fun DiagnosticoPermisos(
    diagnostico: SesionUiState.Diagnostico,
    alProbar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(text = "Diagnóstico de permisos", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Consulta la lista de clientes en el servidor con tu sesión actual.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )
            OutlinedButton(onClick = alProbar, modifier = Modifier.fillMaxWidth()) {
                Text("Probar acceso a clientes")
            }
            when (diagnostico) {
                SesionUiState.Diagnostico.Ninguno -> Unit
                SesionUiState.Diagnostico.Consultando -> Text(
                    text = "Consultando…",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 12.dp)
                )
                is SesionUiState.Diagnostico.Permitido -> Text(
                    text = "Acceso permitido (200): el servidor devolvió ${diagnostico.recurso}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 12.dp)
                )
                is SesionUiState.Diagnostico.Denegado -> Text(
                    text = diagnostico.mensaje,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun AcercaDe(modifier: Modifier = Modifier) {
    val dispositivo = remember { InfoDispositivo() }
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(text = "Acerca de", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Información que entrega el propio dispositivo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )
            FilaInfo("Sistema operativo", dispositivo.sistema)
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
            FilaInfo("Versión", dispositivo.version)
        }
    }
}

@Composable
private fun FilaInfo(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = valor, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun Preferencia(
    titulo: String,
    descripcion: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(18.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = titulo, style = MaterialTheme.typography.titleMedium)
            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
