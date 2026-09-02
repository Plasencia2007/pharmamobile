package pe.edu.upeu.pharmamobile.presentation.pedido

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.math.round
import pe.edu.upeu.pharmamobile.domain.model.Cliente
import pe.edu.upeu.pharmamobile.domain.model.DetallePedido
import pe.edu.upeu.pharmamobile.domain.model.EstadoPedido
import pe.edu.upeu.pharmamobile.domain.model.Pedido
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.presentation.components.DisplayHeading
import pe.edu.upeu.pharmamobile.presentation.components.ErrorSummary
import pe.edu.upeu.pharmamobile.presentation.components.FormButton
import pe.edu.upeu.pharmamobile.presentation.components.PharmaField

@Composable
fun PedidoScreen(onSuccess: (String) -> Unit) {
    var clienteNombre by remember { mutableStateOf("") }
    var clienteCorreo by remember { mutableStateOf("") }
    var productoNombre by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }

    var errorClienteNombre by remember { mutableStateOf<String?>(null) }
    var errorClienteCorreo by remember { mutableStateOf<String?>(null) }
    var errorProducto by remember { mutableStateOf<String?>(null) }
    var errorPrecio by remember { mutableStateOf<String?>(null) }
    var errorCantidad by remember { mutableStateOf<String?>(null) }

    val errores = listOfNotNull(errorClienteNombre, errorClienteCorreo, errorProducto, errorPrecio, errorCantidad)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        DisplayHeading(linea1 = "Registro de", linea2 = "pedido")
        Text(
            text = "Asocia un cliente con el producto que solicita.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )

        PasoTitulo(texto = "01 · Cliente", modifier = Modifier.padding(top = 22.dp))
        Column(
            modifier = Modifier.padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PharmaField(
                value = clienteNombre,
                onValueChange = { clienteNombre = it; errorClienteNombre = null },
                label = "Cliente",
                placeholder = "Nombre del cliente",
                errorMessage = errorClienteNombre
            )
            PharmaField(
                value = clienteCorreo,
                onValueChange = { clienteCorreo = it; errorClienteCorreo = null },
                label = "Correo",
                placeholder = "Correo del cliente",
                errorMessage = errorClienteCorreo,
                keyboardType = KeyboardType.Email
            )
        }

        PasoTitulo(texto = "02 · Producto", modifier = Modifier.padding(top = 22.dp))
        Column(
            modifier = Modifier.padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PharmaField(
                value = productoNombre,
                onValueChange = { productoNombre = it; errorProducto = null },
                label = "Producto",
                placeholder = "Producto",
                errorMessage = errorProducto
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PharmaField(
                    value = precio,
                    onValueChange = { precio = it; errorPrecio = null },
                    label = "Precio",
                    placeholder = "Precio",
                    errorMessage = errorPrecio,
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )
                PharmaField(
                    value = cantidad,
                    onValueChange = { cantidad = it; errorCantidad = null },
                    label = "Cantidad",
                    placeholder = "Cantidad",
                    errorMessage = errorCantidad,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Column(
            modifier = Modifier.padding(top = 22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ErrorSummary(cantidad = errores.size)

            FormButton(
                text = "Registrar pedido",
                onClick = {
                    errorClienteNombre = PedidoValidator.validarNombreCliente(clienteNombre)
                    errorClienteCorreo = PedidoValidator.validarCorreoCliente(clienteCorreo)
                    errorProducto = PedidoValidator.validarProducto(productoNombre)
                    errorPrecio = PedidoValidator.validarPrecio(precio)
                    errorCantidad = PedidoValidator.validarCantidad(cantidad)

                    val esValido = errorClienteNombre == null && errorClienteCorreo == null &&
                        errorProducto == null && errorPrecio == null && errorCantidad == null

                    if (esValido) {
                        val cliente = Cliente(
                            id = 0L,
                            nombre = clienteNombre,
                            correo = clienteCorreo,
                            telefono = null
                        )
                        val producto = Producto(
                            id = 0L,
                            nombre = productoNombre,
                            precio = precio.toDouble(),
                            stock = cantidad.toInt()
                        )
                        val detalle = DetallePedido(producto = producto, cantidad = cantidad.toInt())
                        val pedido = Pedido(
                            id = 0L,
                            cliente = cliente,
                            detalles = listOf(detalle),
                            estado = EstadoPedido.Pendiente
                        )

                        clienteNombre = ""
                        clienteCorreo = ""
                        productoNombre = ""
                        precio = ""
                        cantidad = ""
                        onSuccess("Pedido registrado · Total S/ ${formatMonto(pedido.total())}")
                    }
                }
            )
        }
    }
}

@Composable
private fun PasoTitulo(texto: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = texto.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.secondary
        )
        HorizontalDivider(
            modifier = Modifier.padding(start = 10.dp).weight(1f).height(1.dp)
        )
    }
}

private fun formatMonto(valor: Double): String {
    return (round(valor * 100) / 100).toString()
}
