package pe.edu.upeu.pharmamobile.presentation.producto

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.presentation.components.DisplayHeading
import pe.edu.upeu.pharmamobile.presentation.components.ErrorSummary
import pe.edu.upeu.pharmamobile.presentation.components.FormButton
import pe.edu.upeu.pharmamobile.presentation.components.PharmaChip
import pe.edu.upeu.pharmamobile.presentation.components.PharmaField

private val CATEGORIAS = listOf("Analgésicos", "Antibióticos", "Cuidado")

@Composable
fun ProductoScreen(onSuccess: (String) -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf(CATEGORIAS.first()) }

    var errorNombre by remember { mutableStateOf<String?>(null) }
    var errorPrecio by remember { mutableStateOf<String?>(null) }
    var errorStock by remember { mutableStateOf<String?>(null) }

    val errores = listOfNotNull(errorNombre, errorPrecio, errorStock)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        DisplayHeading(linea1 = "Registro de", linea2 = "producto")
        Text(
            text = "Agrega un nuevo producto al inventario.",
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
                label = "Nombre",
                placeholder = "Ibuprofeno 400 mg",
                errorMessage = errorNombre
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PharmaField(
                    value = precio,
                    onValueChange = { precio = it; errorPrecio = null },
                    label = "Precio",
                    placeholder = "S/ 0.00",
                    errorMessage = errorPrecio,
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )
                PharmaField(
                    value = stock,
                    onValueChange = { stock = it; errorStock = null },
                    label = "Stock",
                    placeholder = "0 uds.",
                    errorMessage = errorStock,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }

            Column {
                Text(
                    text = "CATEGORÍA",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CATEGORIAS.forEach { opcion ->
                        PharmaChip(
                            texto = opcion,
                            seleccionado = categoria == opcion,
                            onClick = { categoria = opcion }
                        )
                    }
                }
            }

            ErrorSummary(cantidad = errores.size)

            FormButton(
                text = "Registrar producto",
                onClick = {
                    errorNombre = ProductoValidator.validarNombre(nombre)
                    errorPrecio = ProductoValidator.validarPrecio(precio)
                    errorStock = ProductoValidator.validarStock(stock)

                    if (errorNombre == null && errorPrecio == null && errorStock == null) {
                        Producto(
                            id = 0L,
                            nombre = nombre,
                            precio = precio.toDouble(),
                            stock = stock.toInt()
                        )
                        nombre = ""
                        precio = ""
                        stock = ""
                        onSuccess("Producto registrado en el inventario")
                    }
                }
            )
        }
    }
}
