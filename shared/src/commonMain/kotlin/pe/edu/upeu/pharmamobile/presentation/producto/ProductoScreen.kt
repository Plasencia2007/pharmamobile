package pe.edu.upeu.pharmamobile.presentation.producto

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.presentation.components.FormButton
import pe.edu.upeu.pharmamobile.presentation.components.FormTextField

@Composable
fun ProductoScreen() {
    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }

    var errorNombre by remember { mutableStateOf<String?>(null) }
    var errorPrecio by remember { mutableStateOf<String?>(null) }
    var errorStock by remember { mutableStateOf<String?>(null) }
    var mensaje by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "PharmaMobile",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Registro de Producto",
                    style = MaterialTheme.typography.titleMedium
                )

                FormTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = "Nombre del producto",
                    errorMessage = errorNombre
                )

                FormTextField(
                    value = precio,
                    onValueChange = { precio = it },
                    label = "Precio",
                    errorMessage = errorPrecio
                )

                FormTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = "Stock",
                    errorMessage = errorStock
                )

                FormButton(
                    text = "Registrar",
                    onClick = {
                        errorNombre = ProductoValidator.validarNombre(nombre)
                        errorPrecio = ProductoValidator.validarPrecio(precio)
                        errorStock = ProductoValidator.validarStock(stock)

                        mensaje = if (errorNombre == null && errorPrecio == null && errorStock == null) {
                            Producto(
                                id = 0L,
                                nombre = nombre,
                                precio = precio.toDouble(),
                                stock = stock.toInt()
                            )
                            "Producto registrado correctamente"
                        } else {
                            ""
                        }
                    }
                )

                if (mensaje.isNotBlank()) {
                    Text(text = mensaje, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Preview
@Composable
private fun ProductoScreenPreview() {
    ProductoScreen()
}
