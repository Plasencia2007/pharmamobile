package pe.edu.upeu.pharmamobile.presentation.config

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobile.platform.InfoDispositivo

@Composable
fun ConfigScreen(
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    alertasStockBajo: Boolean,
    onAlertasStockBajoChange: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
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
                text = "Ana Restrepo",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "Farmacia Central · v1.2.0",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        AcercaDe(modifier = Modifier.padding(top = 22.dp))
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
