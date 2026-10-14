package pe.edu.upeu.pharmamobile.presentation.sesion

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobile.presentation.components.PharmaField
import pe.edu.upeu.pharmamobile.presentation.theme.Amber
import pe.edu.upeu.pharmamobile.presentation.theme.AmberSoft

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
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 48.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                        ),
                        RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Text(
                text = "PharmaMobile",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 18.dp)
            )
            Text(
                text = "Inicia sesión para gestionar el inventario.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp, bottom = 28.dp)
            )

            if (estado.aviso != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 18.dp)
                        .background(AmberSoft, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = Amber)
                    Text(
                        text = estado.aviso,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B4300),
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }
            }

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
                keyboardType = KeyboardType.Password,
                modifier = Modifier.padding(top = 14.dp)
            )

            if (estado.error != null) {
                Text(
                    text = estado.error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 14.dp)
                )
            }

            Button(
                onClick = alIniciar,
                enabled = !estado.cargando,
                modifier = Modifier.padding(top = 24.dp).fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (estado.cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Ingresar", style = MaterialTheme.typography.labelLarge)
                }
            }

            if (depuracion) {
                OutlinedButton(
                    onClick = alProbarSinToken,
                    modifier = Modifier.padding(top = 14.dp).fillMaxWidth()
                ) {
                    Text("Depuración: lanzar petición sin token")
                }
                when (val d = estado.diagnostico) {
                    is SesionUiState.Diagnostico.Denegado -> Text(
                        text = d.mensaje,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    is SesionUiState.Diagnostico.Permitido -> Text(
                        text = "El servidor respondió 200.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                    else -> Unit
                }
            }
        }
    }
}
