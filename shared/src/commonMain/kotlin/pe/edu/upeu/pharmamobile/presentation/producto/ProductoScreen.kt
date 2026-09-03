package pe.edu.upeu.pharmamobile.presentation.producto

import androidx.compose.foundation.background
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.presentation.components.DisplayHeading
import pe.edu.upeu.pharmamobile.presentation.components.ErrorSummary
import pe.edu.upeu.pharmamobile.presentation.components.FormButton
import pe.edu.upeu.pharmamobile.presentation.components.PharmaChip
import pe.edu.upeu.pharmamobile.presentation.components.PharmaField

private val CATEGORIAS = listOf("Analgésicos", "Antibióticos", "Cuidado")

private const val UMBRAL_BAJO_STOCK = 5

// Datos simulados: en un stock igual a 0 el producto se trata como Inactivo
// (no como "Bajo stock"), ya que fuera de venta deja de ser inventario disponible.
fun inventarioSimuladoInicial(): List<Producto> = listOf(
    Producto(id = 1L, nombre = "Paracetamol", precio = 15.50, stock = 100, activo = true),
    Producto(id = 2L, nombre = "Ibuprofeno", precio = 18.90, stock = 50, activo = true),
    Producto(id = 3L, nombre = "Amoxicilina", precio = 25.00, stock = 5, activo = true),
    Producto(id = 4L, nombre = "Loratadina", precio = 12.50, stock = 0, activo = false),
    Producto(id = 5L, nombre = "Diclofenaco", precio = 20.00, stock = 3, activo = true)
)

private val PESTANAS = listOf("Activos", "Inactivos", "Bajo stock")

// Stock igual a 0 se considera Inactivo, sin importar la bandera "activo":
// un producto sin unidades no está disponible para la venta.
private fun Producto.estaInactivo(): Boolean = !activo || stock == 0
private fun Producto.esBajoStock(): Boolean = !estaInactivo() && stock <= UMBRAL_BAJO_STOCK

private fun filtrarInventario(inventario: List<Producto>, pestana: Int): List<Producto> = when (pestana) {
    0 -> inventario.filter { !it.estaInactivo() && !it.esBajoStock() }
    1 -> inventario.filter { it.estaInactivo() }
    2 -> inventario.filter { it.esBajoStock() }
    else -> inventario
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoScreen(
    inventario: SnapshotStateList<Producto>,
    onSuccess: (String) -> Unit
) {
    var tabSeleccionada by remember { mutableStateOf(0) }
    var mostrarFormulario by remember { mutableStateOf(false) }

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
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { mostrarFormulario = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Agregar producto",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        TabRow(
            modifier = Modifier.padding(top = 16.dp),
            selectedTabIndex = tabSeleccionada,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            PESTANAS.forEachIndexed { indice, titulo ->
                Tab(
                    selected = tabSeleccionada == indice,
                    onClick = { tabSeleccionada = indice },
                    text = { Text(titulo) }
                )
            }
        }

        Column(
            modifier = Modifier.padding(top = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val productosFiltrados = filtrarInventario(inventario, tabSeleccionada)
            if (productosFiltrados.isEmpty()) {
                Text(
                    text = "No hay productos en esta categoría.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                productosFiltrados.forEach { producto ->
                    ProductoInventarioItem(producto)
                }
            }
        }
    }

    if (mostrarFormulario) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { mostrarFormulario = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background
        ) {
            FormularioProducto(
                inventario = inventario,
                onSuccess = { mensaje ->
                    mostrarFormulario = false
                    onSuccess(mensaje)
                }
            )
        }
    }
}

@Composable
private fun FormularioProducto(
    inventario: SnapshotStateList<Producto>,
    onSuccess: (String) -> Unit
) {
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
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DisplayHeading(linea1 = "Registro de", linea2 = "producto")
        Text(
            text = "Agrega un nuevo producto al inventario.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

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
                    val siguienteId = (inventario.maxOfOrNull { it.id } ?: 0L) + 1
                    inventario.add(
                        Producto(
                            id = siguienteId,
                            nombre = nombre,
                            precio = precio.toDouble(),
                            stock = stock.toInt()
                        )
                    )
                    onSuccess("Producto registrado en el inventario")
                }
            }
        )
    }
}

@Composable
private fun ProductoInventarioItem(producto: Producto) {
    val colores = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colores.surfaceContainerHighest, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = producto.nombre,
                style = MaterialTheme.typography.titleMedium,
                color = colores.onSurface
            )
            Text(
                text = "S/ ${producto.precio} · Stock: ${producto.stock}",
                style = MaterialTheme.typography.bodyMedium,
                color = colores.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        EstadoBadge(producto)
    }
}

@Composable
private fun EstadoBadge(producto: Producto) {
    val colores = MaterialTheme.colorScheme
    val (texto, color) = when {
        producto.estaInactivo() -> "Inactivo" to colores.onSurfaceVariant
        producto.esBajoStock() -> "Bajo stock" to colores.error
        else -> "Activo" to colores.primary
    }
    Text(
        text = texto,
        style = MaterialTheme.typography.labelLarge,
        color = color,
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}
