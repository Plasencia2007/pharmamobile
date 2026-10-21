package pe.edu.upeu.pharmamobile.presentation.detalle

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.platform.formatearSoles
import pe.edu.upeu.pharmamobile.presentation.components.Capsula
import pe.edu.upeu.pharmamobile.presentation.components.Pildora
import pe.edu.upeu.pharmamobile.presentation.components.colorCapsula
import pe.edu.upeu.pharmamobile.presentation.theme.AmbarTexto
import pe.edu.upeu.pharmamobile.presentation.theme.AmbarTinte
import pe.edu.upeu.pharmamobile.presentation.theme.Coral
import pe.edu.upeu.pharmamobile.presentation.theme.RojoBorde
import pe.edu.upeu.pharmamobile.presentation.theme.Verde
import pe.edu.upeu.pharmamobile.presentation.theme.VerdeTexto
import pe.edu.upeu.pharmamobile.presentation.theme.VerdeTinte
import pe.edu.upeu.pharmamobile.presentation.theme.fuenteMono

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleProductoSheet(
    productoId: Long,
    onCerrar: () -> Unit,
    puedeEscribir: Boolean = false,
    onEditar: (Producto) -> Unit = {},
    onDesactivar: (Producto) -> Unit = {},
    viewModel: DetalleProductoViewModel = koinViewModel(key = "detalle-$productoId") { parametersOf(productoId) }
) {
    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            when (val estado = viewModel.uiState) {
                DetalleProductoUiState.Cargando -> Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) }

                is DetalleProductoUiState.Error -> {
                    Text(estado.mensaje, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                    OutlinedButton(onClick = viewModel::reintentar, shape = RoundedCornerShape(24.dp)) {
                        Text("Reintentar", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                is DetalleProductoUiState.Contenido -> Contenido(
                    producto = estado.producto,
                    precio = estado.ui.precio,
                    puedeEscribir = puedeEscribir,
                    onCompartir = viewModel::compartir,
                    onEditar = { onEditar(estado.producto) },
                    onDesactivar = { onDesactivar(estado.producto) }
                )
            }
        }
    }
}

@Composable
private fun Contenido(
    producto: Producto,
    precio: String,
    puedeEscribir: Boolean,
    onCompartir: () -> Unit,
    onEditar: () -> Unit,
    onDesactivar: () -> Unit
) {
    val colores = MaterialTheme.colorScheme
    val inactivo = producto.estaInactivo()

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Capsula(color = colorCapsula(producto))
                Text(
                    when {
                        inactivo -> "Inactivo"
                        producto.requiereReposicion() -> "Por reponer"
                        else -> "Activo"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = colores.onSurfaceVariant
                )
            }
            IconButton(onClick = onCompartir) {
                Icon(Icons.Filled.Share, contentDescription = "Compartir producto", tint = colores.onSurface)
            }
        }
        Text(producto.nombre, style = MaterialTheme.typography.displaySmall, color = colores.onSurface)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (inactivo) {
                Pildora("No disponible", colores.surfaceContainerHighest, colores.onSurfaceVariant)
            } else {
                Pildora("En venta", VerdeTinte, VerdeTexto, punto = Verde)
            }
            if (producto.pendiente) {
                Pildora("Pendiente de envío · id ${producto.id}", AmbarTinte, AmbarTexto)
            } else {
                Pildora("En PharmaSoft · id ${producto.id}", colores.surfaceContainerHighest, colores.onSurface)
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).border(1.dp, colores.outlineVariant, RoundedCornerShape(16.dp))
    ) {
        Dato("Precio", precio, Modifier.weight(1f))
        Box(Modifier.width(1.dp).height(72.dp).background(colores.outlineVariant))
        Dato("Stock", "${producto.stock} u.", Modifier.weight(1f))
        Box(Modifier.width(1.dp).height(72.dp).background(colores.outlineVariant))
        Dato("Valor", formatearSoles(producto.valorInventario()), Modifier.weight(1f))
    }

    // Barra de nivel: la escala llega a 50 u.; la marca coral es el mínimo de reposición.
    val escala = 50f
    val fraccion = (producto.stock / escala).coerceIn(0f, 1f)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row {
            Text("Nivel de stock", style = MaterialTheme.typography.labelLarge, color = colores.onSurface, modifier = Modifier.weight(1f))
            Text("mínimo ${Producto.STOCK_MINIMO} u.", style = MaterialTheme.typography.bodySmall, color = colores.onSurfaceVariant)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
                .semantics { contentDescription = "${producto.stock} unidades; el mínimo es ${Producto.STOCK_MINIMO}" }
        ) {
            Box(Modifier.align(Alignment.CenterStart).fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(colores.surfaceContainerHighest))
            Box(Modifier.align(Alignment.CenterStart).fillMaxWidth(fraccion).height(10.dp).clip(RoundedCornerShape(5.dp)).background(colores.primary))
            Box(Modifier.fillMaxWidth(Producto.STOCK_MINIMO / escala).fillMaxHeight()) {
                Box(Modifier.align(Alignment.CenterEnd).offset(x = 1.dp).width(2.dp).fillMaxHeight().background(Coral))
            }
        }
    }

    if (puedeEscribir) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onEditar,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colores.primary, contentColor = colores.onPrimary)
            ) { Text("Editar", style = MaterialTheme.typography.titleSmall) }
            if (!inactivo) {
                OutlinedButton(
                    onClick = onDesactivar,
                    modifier = Modifier.height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RojoBorde)
                ) { Text(if (producto.pendiente) "Descartar" else "Desactivar", style = MaterialTheme.typography.titleSmall, color = colores.error) }
            }
        }
    }
}

@Composable
private fun Dato(etiqueta: String, valor: String, modifier: Modifier = Modifier) {
    val colores = MaterialTheme.colorScheme
    Column(modifier = modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(etiqueta, style = MaterialTheme.typography.bodySmall, color = colores.onSurfaceVariant)
        Text(valor, style = MaterialTheme.typography.titleSmall.copy(fontFamily = fuenteMono()), color = colores.onSurface, maxLines = 1)
    }
}
