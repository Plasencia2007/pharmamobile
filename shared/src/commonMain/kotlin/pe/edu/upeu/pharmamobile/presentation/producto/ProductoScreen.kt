package pe.edu.upeu.pharmamobile.presentation.producto

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.presentation.components.Capsula
import pe.edu.upeu.pharmamobile.presentation.components.ErrorSummary
import pe.edu.upeu.pharmamobile.presentation.components.EstadoSincronizacion
import pe.edu.upeu.pharmamobile.presentation.components.FormButton
import pe.edu.upeu.pharmamobile.presentation.components.PharmaChip
import pe.edu.upeu.pharmamobile.presentation.components.PharmaField
import pe.edu.upeu.pharmamobile.presentation.components.colorCapsula
import pe.edu.upeu.pharmamobile.presentation.components.dashedBorder
import pe.edu.upeu.pharmamobile.presentation.detalle.DetalleProductoSheet
import pe.edu.upeu.pharmamobile.presentation.theme.RojoTexto
import pe.edu.upeu.pharmamobile.presentation.theme.fuenteMono

/** Filtros del inventario por estado (la app todavía no guarda la categoría de cada producto). */
private enum class FiltroInventario(val titulo: String, val cumple: (Producto) -> Boolean) {
    Todos("Todos", { true }),
    // Igual que el indicador de Inicio: un producto por reponer sigue a la venta.
    Activos("Activos", { !it.estaInactivo() }),
    PorReponer("Por reponer", { it.requiereReposicion() }),
    Inactivos("Inactivos", { it.estaInactivo() })
}

private val ANCHO_PRECIO = 72.dp
private val ANCHO_STOCK = 64.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoScreen(
    viewModel: ProductoViewModel,
    onSuccess: (String) -> Unit,
    puedeEscribir: Boolean = true,
    solicitudNuevo: Int = 0
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colores = MaterialTheme.colorScheme
    var filtro by remember { mutableStateOf(FiltroInventario.Todos) }
    var busqueda by remember { mutableStateOf("") }
    var mostrarFormulario by remember { mutableStateOf(false) }
    var productoEditando by remember { mutableStateOf<Producto?>(null) }
    var productoAEliminar by remember { mutableStateOf<Producto?>(null) }
    var productoDetalleId by remember { mutableStateOf<Long?>(null) }

    // El botón "+" del dock vive en App: cada pulsación incrementa solicitudNuevo.
    LaunchedEffect(solicitudNuevo) {
        if (solicitudNuevo > 0 && puedeEscribir) {
            productoEditando = null
            mostrarFormulario = true
        }
    }

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
    val todos = (uiState.fase as? ProductoUiState.FaseInventario.ConProductos)?.productos.orEmpty()
    val visibles = todos
        .filter(filtro.cumple)
        .filter { busqueda.isBlank() || it.nombre.contains(busqueda.trim(), ignoreCase = true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(colores.background),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 110.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.Bottom) {
                Text("Inventario", style = MaterialTheme.typography.headlineMedium, color = colores.onSurface, modifier = Modifier.weight(1f))
                Text(
                    "${visibles.size} de ${todos.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colores.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }
        item {
            CampoBusqueda(busqueda, onCambio = { busqueda = it }, modifier = Modifier.padding(top = 14.dp))
        }
        item {
            EstadoSincronizacion(
                sincronizando = uiState.sincronizando,
                aviso = uiState.avisoRed,
                pendientes = uiState.pendientes,
                ultimaSincronizacion = uiState.ultimaSincronizacion,
                onSincronizar = viewModel::sincronizar,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
        if (mensajeOperacionFallida != null) {
            item {
                Text(
                    mensajeOperacionFallida,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colores.error,
                    modifier = Modifier.padding(top = 12.dp).clickable { viewModel.consumirOperacionFallida() }
                )
            }
        }
        item {
            Row(
                modifier = Modifier.padding(top = 14.dp).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FiltroInventario.entries.forEach { opcion ->
                    PharmaChip(
                        texto = opcion.titulo,
                        seleccionado = filtro == opcion,
                        onClick = { filtro = opcion },
                        cuenta = todos.count(opcion.cumple)
                    )
                }
            }
        }
        item {
            Column(modifier = Modifier.padding(top = 14.dp)) {
                Row(modifier = Modifier.padding(start = 38.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("PRODUCTO", style = MaterialTheme.typography.labelSmall, color = colores.onSurfaceVariant, modifier = Modifier.weight(1f))
                    Text("PRECIO", style = MaterialTheme.typography.labelSmall, color = colores.onSurfaceVariant, textAlign = TextAlign.End, modifier = Modifier.width(ANCHO_PRECIO))
                    Text("STOCK", style = MaterialTheme.typography.labelSmall, color = colores.onSurfaceVariant, textAlign = TextAlign.End, modifier = Modifier.width(ANCHO_STOCK))
                }
                HorizontalDivider(color = colores.outlineVariant)
            }
        }

        when (uiState.fase) {
            is ProductoUiState.FaseInventario.Cargando -> item {
                Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colores.primary)
                }
            }
            is ProductoUiState.FaseInventario.SinProductos -> item {
                // Primera apertura sin red: no es un error, solo no hay nada que mostrar todavía.
                TextoVacio(
                    if (uiState.ultimaSincronizacion == null) {
                        "Este teléfono aún no tiene el inventario. Conéctate a internet y pulsa Sincronizar para descargarlo."
                    } else {
                        "Aún no hay productos. Registra el primero con el botón +."
                    }
                )
            }
            is ProductoUiState.FaseInventario.ConProductos -> {
                if (visibles.isEmpty()) {
                    item { TextoVacio("No hay productos que coincidan.") }
                } else {
                    items(visibles, key = { it.id }) { producto ->
                        FilaProducto(
                            producto = producto,
                            procesando = operacion is ProductoUiState.Operacion.EnCurso && operacion.productoId == producto.id,
                            onClick = { productoDetalleId = producto.id }
                        )
                    }
                }
            }
        }
    }

    if (mostrarFormulario) {
        ModalBottomSheet(
            onDismissRequest = {
                mostrarFormulario = false
                productoEditando = null
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colores.background
        ) {
            FormularioProducto(
                formulario = uiState.formulario,
                productoEditando = productoEditando,
                onRegistrar = { nombre, precio, stock -> viewModel.registrarProducto(nombre, precio, stock) },
                onActualizar = { producto, nombre, precio, stock -> viewModel.actualizarProducto(producto, nombre, precio, stock) }
            )
        }
    }

    productoDetalleId?.let { id ->
        DetalleProductoSheet(
            productoId = id,
            onCerrar = { productoDetalleId = null },
            puedeEscribir = puedeEscribir,
            onEditar = { producto ->
                productoDetalleId = null
                productoEditando = producto
                mostrarFormulario = true
            },
            onDesactivar = { producto ->
                productoDetalleId = null
                productoAEliminar = producto
            }
        )
    }

    productoAEliminar?.let { producto ->
        AlertDialog(
            onDismissRequest = { productoAEliminar = null },
            containerColor = colores.background,
            title = { Text(if (producto.pendiente) "Descartar producto" else "Desactivar producto", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    if (producto.pendiente) {
                        "\"${producto.nombre}\" todavía no se envió a PharmaSoft. Se borrará del teléfono."
                    } else {
                        "\"${producto.nombre}\" dejará de estar disponible para la venta."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarProducto(producto.id)
                    productoAEliminar = null
                }) { Text(if (producto.pendiente) "Descartar" else "Desactivar", color = colores.error, style = MaterialTheme.typography.labelLarge) }
            },
            dismissButton = {
                TextButton(onClick = { productoAEliminar = null }) {
                    Text("Cancelar", color = colores.onSurface, style = MaterialTheme.typography.labelLarge)
                }
            }
        )
    }
}

@Composable
private fun CampoBusqueda(valor: String, onCambio: (String) -> Unit, modifier: Modifier = Modifier) {
    val colores = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, colores.outline, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Filled.Search, contentDescription = null, tint = colores.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (valor.isEmpty()) {
                Text("Buscar por nombre", style = MaterialTheme.typography.bodyMedium, color = colores.onSurfaceVariant.copy(alpha = 0.8f))
            }
            BasicTextField(
                value = valor,
                onValueChange = onCambio,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = colores.onSurface),
                cursorBrush = SolidColor(colores.primary),
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Buscar producto" }
            )
        }
    }
}

@Composable
private fun FilaProducto(producto: Producto, procesando: Boolean, onClick: () -> Unit) {
    val colores = MaterialTheme.colorScheme
    val bajo = producto.requiereReposicion()
    val inactivo = producto.estaInactivo()
    val subtitulo = when {
        producto.pendiente -> "Pendiente de envío · id local ${producto.id}"
        bajo -> "Por reponer · mínimo ${Producto.STOCK_MINIMO}"
        inactivo -> "Inactivo"
        else -> "Activo"
    }
    val contenedor = if (producto.pendiente) {
        Modifier
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colores.surfaceVariant)
            .dashedBorder(colores.onSurfaceVariant, 12.dp, 1.dp)
            .padding(horizontal = 10.dp)
    } else {
        Modifier
    }
    Column(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = contenedor.fillMaxWidth().padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Capsula(color = colorCapsula(producto))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    producto.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (inactivo) colores.onSurfaceVariant else colores.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(subtitulo, style = MaterialTheme.typography.bodySmall, color = if (bajo) RojoTexto else colores.onSurfaceVariant)
            }
            if (procesando) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = colores.primary)
            } else {
                Text(
                    formatearPrecio(producto.precio),
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = fuenteMono()),
                    color = colores.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(ANCHO_PRECIO)
                )
                Text(
                    "${producto.stock}",
                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = fuenteMono()),
                    color = when {
                        bajo -> RojoTexto
                        inactivo -> colores.onSurfaceVariant
                        else -> colores.onSurface
                    },
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(ANCHO_STOCK)
                )
            }
        }
        if (!producto.pendiente) HorizontalDivider(color = colores.outlineVariant.copy(alpha = 0.6f))
    }
}

@Composable
private fun TextoVacio(texto: String) {
    Text(
        texto,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 24.dp)
    )
}

/** Precio con dos decimales para la columna de la tabla (el detalle usa el formato de moneda completo). */
private fun formatearPrecio(precio: Double): String {
    val centimos = kotlin.math.round(precio * 100).toLong()
    val decimales = (centimos % 100).toString().padStart(2, '0')
    return "${centimos / 100}.$decimales"
}

@Composable
private fun FormularioProducto(
    formulario: ProductoUiState.FormularioState,
    productoEditando: Producto?,
    onRegistrar: (nombre: String, precio: String, stock: String) -> Unit,
    onActualizar: (producto: Producto, nombre: String, precio: String, stock: String) -> Unit
) {
    val colores = MaterialTheme.colorScheme
    var nombre by remember(productoEditando) { mutableStateOf(productoEditando?.nombre ?: "") }
    var precio by remember(productoEditando) { mutableStateOf(productoEditando?.precio?.toString() ?: "") }
    var stock by remember(productoEditando) { mutableStateOf(productoEditando?.stock?.toString() ?: "") }
    val errores = listOfNotNull(formulario.errorNombre, formulario.errorPrecio, formulario.errorStock)
    val editando = productoEditando != null

    Column(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(if (editando) "Editar producto" else "Nuevo producto", style = MaterialTheme.typography.headlineMedium, color = colores.onSurface)
            Text(
                if (editando) "Actualiza los datos de \"${productoEditando.nombre}\"."
                else "Se guarda primero en el teléfono y se envía a PharmaSoft cuando hay conexión.",
                style = MaterialTheme.typography.bodyMedium,
                color = colores.onSurfaceVariant
            )
        }

        PharmaField(
            value = nombre,
            onValueChange = { nombre = it },
            label = "Nombre comercial",
            placeholder = "Ibuprofeno 400mg",
            errorMessage = formulario.errorNombre,
            ayuda = "Incluye la concentración, por ejemplo 400mg."
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PharmaField(
                value = precio,
                onValueChange = { precio = it },
                label = "Precio",
                placeholder = "0.00",
                errorMessage = formulario.errorPrecio,
                keyboardType = KeyboardType.Decimal,
                prefijo = "S/",
                modifier = Modifier.weight(1f)
            )
            PharmaField(
                value = stock,
                onValueChange = { stock = it },
                label = if (editando) "Stock" else "Stock inicial",
                placeholder = "0",
                errorMessage = formulario.errorStock,
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f)
            )
        }

        ErrorSummary(cantidad = errores.size)

        FormButton(
            text = when {
                formulario.enviando && editando -> "Actualizando…"
                formulario.enviando -> "Guardando…"
                editando -> "Guardar cambios"
                else -> "Guardar producto"
            },
            enabled = !formulario.enviando,
            onClick = {
                val actual = productoEditando
                if (actual != null) onActualizar(actual, nombre, precio, stock) else onRegistrar(nombre, precio, stock)
            }
        )
    }
}
