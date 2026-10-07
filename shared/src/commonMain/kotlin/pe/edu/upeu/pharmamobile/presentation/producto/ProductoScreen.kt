package pe.edu.upeu.pharmamobile.presentation.producto

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.presentation.components.DisplayHeading
import pe.edu.upeu.pharmamobile.presentation.components.ErrorSummary
import pe.edu.upeu.pharmamobile.presentation.components.FormButton
import pe.edu.upeu.pharmamobile.presentation.components.PharmaChip
import pe.edu.upeu.pharmamobile.presentation.components.PharmaField
import pe.edu.upeu.pharmamobile.presentation.detalle.DetalleProductoSheet
import pe.edu.upeu.pharmamobile.presentation.theme.Amber
import pe.edu.upeu.pharmamobile.presentation.theme.Coral
import pe.edu.upeu.pharmamobile.presentation.theme.Emerald

private val CATEGORIAS = listOf("Analgésicos", "Antibióticos", "Cuidado")
private val PESTANAS = listOf("Activos", "Inactivos", "Bajo stock")

private fun filtrarInventario(inventario: List<Producto>, pestana: Int): List<Producto> = when (pestana) {
    0 -> inventario.filter { !it.estaInactivo() && !it.requiereReposicion() }
    1 -> inventario.filter { it.estaInactivo() }
    2 -> inventario.filter { it.requiereReposicion() }
    else -> inventario
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoScreen(
    viewModel: ProductoViewModel,
    onSuccess: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var tabSeleccionada by remember { mutableStateOf(0) }
    var mostrarFormulario by remember { mutableStateOf(false) }
    var productoEditando by remember { mutableStateOf<Producto?>(null) }
    var productoAEliminar by remember { mutableStateOf<Producto?>(null) }
    var productoDetalleId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(uiState.formulario.mensajeExito) {
        uiState.formulario.mensajeExito?.let { mensaje ->
            mostrarFormulario = false
            productoEditando = null
            onSuccess(mensaje)
            viewModel.consumirMensajeExito()
        }
    }

    val operacion = uiState.operacion
    val mensajeOperacionFallida = (operacion as? ProductoUiState.Operacion.Fallida)?.mensaje

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Inventario",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Clasifica tus productos por estado.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Coral)
                    .clickable {
                        productoEditando = null
                        mostrarFormulario = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Agregar producto",
                    tint = Color.White
                )
            }
        }

        if (mensajeOperacionFallida != null) {
            Text(
                text = mensajeOperacionFallida,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .clickable { viewModel.consumirOperacionFallida() }
            )
        }

        PestanasSegmentadas(
            seleccionada = tabSeleccionada,
            onSeleccionar = { tabSeleccionada = it },
            modifier = Modifier.padding(top = 16.dp)
        )

        Column(
            modifier = Modifier.padding(top = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (val fase = uiState.fase) {
                is ProductoUiState.FaseInventario.Cargando -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is ProductoUiState.FaseInventario.SinProductos -> {
                    Text(
                        text = "Aún no hay productos registrados.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                is ProductoUiState.FaseInventario.Error -> {
                    Text(
                        text = fase.mensaje,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is ProductoUiState.FaseInventario.ConProductos -> {
                    val productosFiltrados = filtrarInventario(fase.productos, tabSeleccionada)
                    if (productosFiltrados.isEmpty()) {
                        Text(
                            text = "No hay productos en esta categoría.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        productosFiltrados.forEach { producto ->
                            val procesando = operacion is ProductoUiState.Operacion.EnCurso &&
                                operacion.productoId == producto.id
                            ProductoInventarioItem(
                                producto = producto,
                                procesando = procesando,
                                onVer = { productoDetalleId = producto.id },
                                onEditar = {
                                    productoEditando = producto
                                    mostrarFormulario = true
                                },
                                onEliminar = { productoAEliminar = producto }
                            )
                        }
                    }
                }
            }
        }
    }

    if (mostrarFormulario) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = {
                mostrarFormulario = false
                productoEditando = null
            },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background
        ) {
            FormularioProducto(
                formulario = uiState.formulario,
                productoEditando = productoEditando,
                onRegistrar = { nombre, precio, stock -> viewModel.registrarProducto(nombre, precio, stock) },
                onActualizar = { producto, nombre, precio, stock ->
                    viewModel.actualizarProducto(producto, nombre, precio, stock)
                }
            )
        }
    }

    productoDetalleId?.let { id ->
        DetalleProductoSheet(productoId = id, onCerrar = { productoDetalleId = null })
    }

    productoAEliminar?.let { producto ->
        AlertDialog(
            onDismissRequest = { productoAEliminar = null },
            title = { Text("Eliminar producto") },
            text = { Text("¿Seguro que quieres eliminar \"${producto.nombre}\" del inventario?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarProducto(producto.id)
                    productoAEliminar = null
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { productoAEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun FormularioProducto(
    formulario: ProductoUiState.FormularioState,
    productoEditando: Producto?,
    onRegistrar: (nombre: String, precio: String, stock: String) -> Unit,
    onActualizar: (producto: Producto, nombre: String, precio: String, stock: String) -> Unit
) {
    var nombre by remember(productoEditando) { mutableStateOf(productoEditando?.nombre ?: "") }
    var precio by remember(productoEditando) { mutableStateOf(productoEditando?.precio?.toString() ?: "") }
    var stock by remember(productoEditando) { mutableStateOf(productoEditando?.stock?.toString() ?: "") }
    var categoria by remember { mutableStateOf(CATEGORIAS.first()) }

    val errores = listOfNotNull(formulario.errorNombre, formulario.errorPrecio, formulario.errorStock)
    val editando = productoEditando != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DisplayHeading(
            linea1 = if (editando) "Editar" else "Registro de",
            linea2 = "producto"
        )
        Text(
            text = if (editando) {
                "Actualiza los datos de \"${productoEditando.nombre}\"."
            } else {
                "Agrega un nuevo producto al inventario."
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        PharmaField(
            value = nombre,
            onValueChange = { nombre = it },
            label = "Nombre",
            placeholder = "Ibuprofeno 400 mg",
            errorMessage = formulario.errorNombre
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PharmaField(
                value = precio,
                onValueChange = { precio = it },
                label = "Precio",
                placeholder = "S/ 0.00",
                errorMessage = formulario.errorPrecio,
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.weight(1f)
            )
            PharmaField(
                value = stock,
                onValueChange = { stock = it },
                label = "Stock",
                placeholder = "0 uds.",
                errorMessage = formulario.errorStock,
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
            text = when {
                formulario.enviando && editando -> "Actualizando..."
                formulario.enviando -> "Registrando..."
                editando -> "Actualizar producto"
                else -> "Registrar producto"
            },
            onClick = {
                val actual = productoEditando
                if (actual != null) {
                    onActualizar(actual, nombre, precio, stock)
                } else {
                    onRegistrar(nombre, precio, stock)
                }
            }
        )
    }
}

@Composable
private fun PestanasSegmentadas(
    seleccionada: Int,
    onSeleccionar: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colores = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colores.surfaceVariant, RoundedCornerShape(999.dp))
            .padding(4.dp)
    ) {
        PESTANAS.forEachIndexed { indice, titulo ->
            val activa = indice == seleccionada
            val acento = colorDePestana(indice)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (activa) acento else Color.Transparent)
                    .clickable { onSeleccionar(indice) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (activa) Color.White else colores.onSurfaceVariant
                )
            }
        }
    }
}

private fun colorDePestana(indice: Int): Color = when (indice) {
    0 -> Emerald
    1 -> Color(0xFF69766F)
    else -> Amber
}

@Composable
private fun ProductoInventarioItem(
    producto: Producto,
    procesando: Boolean,
    onVer: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val colores = MaterialTheme.colorScheme
    val acento = colorDeEstado(producto)
    val ui = remember(producto) { producto.toUi() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colores.surface, RoundedCornerShape(16.dp))
            .border(1.dp, colores.outline.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onVer)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(acento.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = producto.nombre.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = acento
            )
        }
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp, end = 8.dp)) {
            Text(
                text = producto.nombre,
                style = MaterialTheme.typography.titleMedium,
                color = colores.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${ui.precio} · Stock: ${ui.stock}",
                style = MaterialTheme.typography.bodyMedium,
                color = colores.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            EstadoBadge(producto)
            if (procesando) {
                Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = colores.primary
                    )
                }
            } else {
                Row(modifier = Modifier.padding(top = 2.dp)) {
                    IconButton(onClick = onEditar, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Editar ${producto.nombre}",
                            tint = colores.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onEliminar, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Eliminar ${producto.nombre}",
                            tint = colores.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

internal fun colorDeEstado(producto: Producto): Color = when {
    producto.estaInactivo() -> Color(0xFF69766F)
    producto.requiereReposicion() -> Amber
    else -> Emerald
}

@Composable
internal fun EstadoBadge(producto: Producto) {
    val texto = when {
        producto.estaInactivo() -> "Inactivo"
        producto.requiereReposicion() -> "Bajo stock"
        else -> "Activo"
    }
    val color = colorDeEstado(producto)
    Text(
        text = texto,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color = Color.White,
        modifier = Modifier
            .padding(end = 4.dp)
            .background(color, RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}
