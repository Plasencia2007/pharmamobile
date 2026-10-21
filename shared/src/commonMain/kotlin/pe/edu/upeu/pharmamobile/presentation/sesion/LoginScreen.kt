package pe.edu.upeu.pharmamobile.presentation.sesion

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobile.presentation.components.Capsula
import pe.edu.upeu.pharmamobile.presentation.components.FormButton
import pe.edu.upeu.pharmamobile.presentation.components.PharmaField
import pe.edu.upeu.pharmamobile.presentation.theme.AmbarBorde
import pe.edu.upeu.pharmamobile.presentation.theme.AmbarTexto
import pe.edu.upeu.pharmamobile.presentation.theme.AmbarTexto2
import pe.edu.upeu.pharmamobile.presentation.theme.AmbarTinte
import pe.edu.upeu.pharmamobile.presentation.theme.Coral
import pe.edu.upeu.pharmamobile.presentation.theme.fuenteMono

/**
 * El composable no conoce el token ni HTTP: solo muestra el estado y avisa
 * de lo que el usuario hace. El botón se deshabilita mientras [SesionUiState.cargando].
 */
@Composable
fun LoginScreen(
    estado: SesionUiState,
    alCambiarUsuario: (String) -> Unit,
    alCambiarClave: (String) -> Unit,
    alIniciar: () -> Unit,
    depuracion: Boolean = false,
    alProbarSinToken: () -> Unit = {}
) {
    val colores = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colores.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 28.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Capsula(color = Coral, ancho = 34.dp, alto = 16.dp, grosor = 2.dp, giro = -30f)
            Text("PharmaMobile", style = MaterialTheme.typography.titleLarge, color = colores.onSurface)
        }

        Spacer(Modifier.height(72.dp))

        Text("Inicia sesión en tu farmacia", style = MaterialTheme.typography.displaySmall, color = colores.onSurface)
        Text(
            "Usa tu cuenta de PharmaSoft. El inventario queda disponible también sin conexión.",
            style = MaterialTheme.typography.bodyLarge,
            color = colores.onSurfaceVariant,
            modifier = Modifier.padding(top = 10.dp, bottom = 28.dp)
        )

        if (estado.aviso != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AmbarTinte)
                    .border(1.dp, AmbarBorde, RoundedCornerShape(14.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = AmbarTexto2, modifier = Modifier.size(20.dp))
                Text(
                    estado.aviso,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AmbarTexto,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            PharmaField(
                value = estado.usuario,
                onValueChange = alCambiarUsuario,
                label = "Usuario",
                placeholder = "admin",
                errorMessage = estado.errorUsuario
            )
            PharmaField(
                value = estado.clave,
                onValueChange = alCambiarClave,
                label = "Contraseña",
                placeholder = "••••••••",
                errorMessage = estado.errorClave,
                esClave = true,
                keyboardType = KeyboardType.Password
            )
        }

        if (estado.error != null) {
            Text(
                estado.error,
                style = MaterialTheme.typography.bodyMedium,
                color = colores.error,
                modifier = Modifier.padding(top = 14.dp)
            )
        }

        Box(modifier = Modifier.padding(top = 24.dp)) {
            if (estado.cargando) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(26.dp)).background(colores.primary),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = colores.onPrimary)
                }
            } else {
                FormButton(text = "Iniciar sesión", onClick = alIniciar)
            }
        }

        if (depuracion) {
            OutlinedButton(
                onClick = alProbarSinToken,
                modifier = Modifier.padding(top = 12.dp).fillMaxWidth().heightIn(min = 48.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Depuración: lanzar petición sin token", style = MaterialTheme.typography.labelLarge, color = colores.onSurface)
            }
            when (val d = estado.diagnostico) {
                is SesionUiState.Diagnostico.Denegado -> Text(
                    d.mensaje,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colores.error,
                    modifier = Modifier.padding(top = 10.dp)
                )
                is SesionUiState.Diagnostico.Permitido -> Text(
                    "El servidor respondió 200.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 10.dp)
                )
                else -> Unit
            }
        }

        Spacer(Modifier.height(40.dp))
        HorizontalDivider(color = colores.outlineVariant)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Servidor: PharmaSoft", style = MaterialTheme.typography.bodySmall, color = colores.onSurfaceVariant)
            Text("v1.4.0", style = MaterialTheme.typography.bodySmall.copy(fontFamily = fuenteMono()), color = colores.onSurfaceVariant)
        }
    }
}
