package pe.edu.upeu.pharmamobile.presentation.cliente

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobile.domain.model.Cliente
import pe.edu.upeu.pharmamobile.presentation.components.DisplayHeading
import pe.edu.upeu.pharmamobile.presentation.components.ErrorSummary
import pe.edu.upeu.pharmamobile.presentation.components.FormButton
import pe.edu.upeu.pharmamobile.presentation.components.PharmaField

@Composable
fun ClienteScreen(onSuccess: (String) -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }

    var errorNombre by remember { mutableStateOf<String?>(null) }
    var errorCorreo by remember { mutableStateOf<String?>(null) }
    var errorTelefono by remember { mutableStateOf<String?>(null) }

    val errores = listOfNotNull(errorNombre, errorCorreo, errorTelefono)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        DisplayHeading(linea1 = "Registro de", linea2 = "cliente")
        Text(
            text = "Agrega un cliente a tu cartera.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )

        Column(
            modifier = Modifier.padding(top = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PharmaField(
                value = nombre,
                onValueChange = { nombre = it; errorNombre = null },
                label = "Nombre completo",
                placeholder = "María Restrepo",
                errorMessage = errorNombre
            )

            PharmaField(
                value = correo,
                onValueChange = { correo = it; errorCorreo = null },
                label = "Correo electrónico",
                placeholder = "maria@correo.com",
                errorMessage = errorCorreo,
                keyboardType = KeyboardType.Email
            )

            PharmaField(
                value = telefono,
                onValueChange = { telefono = it; errorTelefono = null },
                label = "Teléfono",
                placeholder = "+51 900 000 000",
                errorMessage = errorTelefono,
                optional = true,
                keyboardType = KeyboardType.Phone
            )

            ErrorSummary(cantidad = errores.size)

            FormButton(
                text = "Registrar cliente",
                onClick = {
                    errorNombre = ClienteValidator.validarNombre(nombre)
                    errorCorreo = ClienteValidator.validarCorreo(correo)
                    errorTelefono = ClienteValidator.validarTelefono(telefono)

                    if (errorNombre == null && errorCorreo == null && errorTelefono == null) {
                        Cliente(
                            id = 0L,
                            nombre = nombre,
                            correo = correo,
                            telefono = telefono.ifBlank { null }
                        )
                        nombre = ""
                        correo = ""
                        telefono = ""
                        onSuccess("Cliente agregado a tu cartera")
                    }
                }
            )
        }
    }
}
