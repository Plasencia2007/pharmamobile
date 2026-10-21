package pe.edu.upeu.pharmamobile.presentation.config

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobile.domain.model.Sesion
import pe.edu.upeu.pharmamobile.platform.InfoDispositivo
import pe.edu.upeu.pharmamobile.presentation.sesion.SesionUiState
import pe.edu.upeu.pharmamobile.presentation.theme.RojoBorde
import pe.edu.upeu.pharmamobile.presentation.theme.fuenteMono

@Composable
fun ConfigScreen(
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    alertasStockBajo: Boolean,
    onAlertasStockBajoChange: (Boolean) -> Unit,
    sesion: Sesion?,
    diagnostico: SesionUiState.Diagnostico,
    alProbarClientes: () -> Unit,
    alCerrarSesion: () -> Unit,
    depuracion: Boolean,
    alAlterarToken: () -> Unit,
    onVolver: () -> Unit = {},
    guardados: Int = 0,
    porEnviar: Int = 0
) {
    val colores = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onVolver, modifier = Modifier.padding(end = 4.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = colores.onSurface)
            }
            Text("Ajustes", style = MaterialTheme.typography.headlineMedium, color = colores.onSurface)
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(
                modifier = Modifier.size(52.dp).clip(CircleShape).background(colores.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(iniciales(sesion?.nombreCompleto.orEmpty()), style = MaterialTheme.typography.titleMedium, color = colores.onPrimary)
            }
            Column {
                Text(sesion?.nombreCompleto ?: "Sin sesión", style = MaterialTheme.typography.titleLarge, color = colores.onSurface)
                Text(
                    sesion?.let { "@${it.usuario} · ${it.permisos.size} permisos" } ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colores.onSurfaceVariant
                )
            }
        }

        Seccion("Sincronización") {
            FilaInfo("Productos guardados en el teléfono", "$guardados")
            HorizontalDivider(color = colores.outlineVariant)
            FilaInfo("Cambios por enviar", "$porEnviar")
        }

        Seccion("Preferencias") {
            Preferencia("Tema oscuro", "Cambia la apariencia de toda la app", darkTheme, onDarkThemeChange)
            HorizontalDivider(color = colores.outlineVariant)
            Preferencia("Alertas de stock bajo", "Avisar cuando queden menos de 10 uds.", alertasStockBajo, onAlertasStockBajoChange)
        }

        DiagnosticoPermisos(diagnostico = diagnostico, alProbar = alProbarClientes)

        if (depuracion) {
            OutlinedButton(
                onClick = alAlterarToken,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Depuración: alterar un carácter del token", style = MaterialTheme.typography.labelLarge, color = colores.onSurface)
            }
        }

        AcercaDe()

        OutlinedButton(
            onClick = alCerrarSesion,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(26.dp),
            border = BorderStroke(1.dp, RojoBorde)
        ) {
            Text("Cerrar sesión", style = MaterialTheme.typography.titleSmall, color = colores.error)
        }
    }
}

@Composable
private fun Seccion(titulo: String, contenido: @Composable () -> Unit) {
    val colores = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(titulo.uppercase(), style = MaterialTheme.typography.labelSmall, color = colores.onSurfaceVariant)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, colores.outlineVariant, RoundedCornerShape(16.dp))
        ) { contenido() }
    }
}

/** Pide un recurso protegido para observar qué responde el servidor según el rol. */
@Composable
private fun DiagnosticoPermisos(
    diagnostico: SesionUiState.Diagnostico,
    alProbar: () -> Unit
) {
    Seccion("Diagnóstico de permisos") {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Consulta la lista de clientes en el servidor con tu sesión actual.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            OutlinedButton(onClick = alProbar, modifier = Modifier.fillMaxWidth().height(44.dp), shape = RoundedCornerShape(22.dp)) {
                Text("Probar acceso a clientes", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
            }
            when (diagnostico) {
                SesionUiState.Diagnostico.Ninguno -> Unit
                SesionUiState.Diagnostico.Consultando -> Text(
                    text = "Consultando…",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 12.dp)
                )
                is SesionUiState.Diagnostico.Informacion -> Text(
                    text = diagnostico.mensaje,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 12.dp)
                )
                is SesionUiState.Diagnostico.Permitido -> Text(
                    text = "Acceso permitido (200): el servidor devolvió ${diagnostico.recurso}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
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
private fun AcercaDe() {
    val dispositivo = remember { InfoDispositivo() }
    Seccion("Acerca de") {
        FilaInfo("Sistema operativo", dispositivo.sistema)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        FilaInfo("Versión", dispositivo.version)
    }
}

@Composable
private fun FilaInfo(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.titleSmall.copy(fontFamily = fuenteMono()),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun Preferencia(
    titulo: String,
    descripcion: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colores = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = titulo, style = MaterialTheme.typography.titleMedium, color = colores.onSurface)
            Text(
                text = descripcion,
                style = MaterialTheme.typography.bodySmall,
                color = colores.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = colores.primary, checkedThumbColor = colores.onPrimary)
        )
    }
}

private fun iniciales(nombre: String): String =
    nombre.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }.ifEmpty { "?" }
