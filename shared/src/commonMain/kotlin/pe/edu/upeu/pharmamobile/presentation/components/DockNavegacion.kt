package pe.edu.upeu.pharmamobile.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobile.presentation.theme.Blanco
import pe.edu.upeu.pharmamobile.presentation.theme.Coral
import pe.edu.upeu.pharmamobile.presentation.theme.Tinta

/** Una sección del dock: su ícono se dibuja con el color que le pasa el dock. */
class ItemDock(
    val id: String,
    val titulo: String,
    val icono: @Composable (Color) -> Unit
)

/**
 * Barra de navegación flotante: una píldora negra con la sección activa
 * resaltada (ícono + nombre) y las demás solo con ícono; el botón "+" va
 * aparte. Es lo que se conservó de la segunda propuesta de diseño.
 */
@Composable
fun DockNavegacion(
    items: List<ItemDock>,
    seleccionado: String,
    onSeleccionar: (String) -> Unit,
    onNuevo: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.weight(1f).height(64.dp).shadow(12.dp, RoundedCornerShape(32.dp)),
            shape = RoundedCornerShape(32.dp),
            color = Tinta
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp).selectableGroup(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val activo = item.id == seleccionado
                    Row(
                        modifier = Modifier
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (activo) Blanco else Color.Transparent)
                            .selectable(selected = activo, role = Role.Tab, onClick = { onSeleccionar(item.id) })
                            .semantics { contentDescription = item.titulo }
                            .padding(horizontal = if (activo) 14.dp else 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item.icono(if (activo) Tinta else Blanco)
                        if (activo) {
                            Text(item.titulo, style = MaterialTheme.typography.titleSmall, color = Tinta)
                        }
                    }
                }
            }
        }
        if (onNuevo != null) {
            IconButton(
                onClick = onNuevo,
                modifier = Modifier.size(64.dp).shadow(12.dp, CircleShape),
                colors = IconButtonDefaults.iconButtonColors(containerColor = Coral, contentColor = Blanco)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Nuevo producto", modifier = Modifier.size(28.dp))
            }
        }
    }
}

/** Ícono de cápsula en contorno para la sección Inventario del dock. */
@Composable
fun IconoCapsula(color: Color) {
    Box(Modifier.size(22.dp), contentAlignment = Alignment.Center) {
        Capsula(color = Color.Transparent, ancho = 20.dp, alto = 9.dp, contorno = color, grosor = 1.8.dp, giro = -30f)
    }
}
