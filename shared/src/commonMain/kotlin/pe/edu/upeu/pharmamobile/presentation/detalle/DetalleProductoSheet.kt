package pe.edu.upeu.pharmamobile.presentation.detalle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.presentation.producto.EstadoBadge
import pe.edu.upeu.pharmamobile.presentation.producto.colorDeEstado

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleProductoSheet(
    productoId: Long,
    onCerrar: () -> Unit,
    viewModel: DetalleProductoViewModel = koinViewModel(key = "detalle-$productoId") { parametersOf(productoId) }
) {
    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val estado = viewModel.uiState) {
                DetalleProductoUiState.Cargando -> Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }

                is DetalleProductoUiState.Error -> {
                    Text(estado.mensaje, color = MaterialTheme.colorScheme.error)
                    Button(onClick = viewModel::reintentar) { Text("Reintentar") }
                }

                is DetalleProductoUiState.Contenido -> Contenido(
                    producto = estado.producto,
                    nombre = estado.ui.nombre,
                    precio = estado.ui.precio,
                    stock = estado.ui.stock,
                    onCompartir = viewModel::compartir
                )
            }
            Spacer(Modifier.size(16.dp))
        }
    }
}

@Composable
private fun Contenido(
    producto: Producto,
    nombre: String,
    precio: String,
    stock: Int,
    onCompartir: () -> Unit
) {
    val colores = MaterialTheme.colorScheme
    val acento = colorDeEstado(producto)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(acento.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Text(nombre.take(1).uppercase(), style = MaterialTheme.typography.headlineSmall, color = acento)
        }
        Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
            Text(nombre, style = MaterialTheme.typography.titleLarge, color = colores.onSurface)
            Text("Detalle del producto", style = MaterialTheme.typography.bodyMedium, color = colores.onSurfaceVariant)
        }
        EstadoBadge(producto)
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Dato("Precio", precio, Modifier.weight(1f))
        Dato("Stock", "$stock uds.", Modifier.weight(1f))
    }
    Button(
        onClick = onCompartir,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = colores.primary, contentColor = colores.onPrimary)
    ) {
        Icon(Icons.Default.Share, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Compartir", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
private fun Dato(etiqueta: String, valor: String, modifier: Modifier = Modifier) {
    val colores = MaterialTheme.colorScheme
    Column(
        modifier = modifier.clip(RoundedCornerShape(16.dp)).background(colores.surfaceVariant).padding(16.dp)
    ) {
        Text(etiqueta, style = MaterialTheme.typography.labelSmall, color = colores.onSurfaceVariant)
        Text(valor, style = MaterialTheme.typography.titleLarge, color = colores.onSurface)
    }
}
