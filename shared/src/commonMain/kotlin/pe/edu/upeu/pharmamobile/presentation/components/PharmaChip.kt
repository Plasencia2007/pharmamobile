package pe.edu.upeu.pharmamobile.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobile.presentation.theme.fuenteMono

/** Filtro en píldora: el seleccionado va en tinta; [cuenta] se muestra en monoespaciada. */
@Composable
fun PharmaChip(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit,
    cuenta: Int? = null
) {
    val colores = MaterialTheme.colorScheme
    val forma = RoundedCornerShape(18.dp)
    val fondo = if (seleccionado) colores.primary else colores.background
    val textoColor = if (seleccionado) colores.onPrimary else colores.onSurface
    Row(
        modifier = Modifier
            .height(36.dp)
            .clip(forma)
            .background(fondo)
            .border(1.dp, if (seleccionado) colores.primary else colores.outline, forma)
            .selectable(selected = seleccionado, role = Role.Tab, onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(texto, style = MaterialTheme.typography.labelLarge, color = textoColor)
        if (cuenta != null) {
            Text(
                "$cuenta",
                style = MaterialTheme.typography.labelMedium.copy(fontFamily = fuenteMono()),
                color = textoColor.copy(alpha = 0.75f)
            )
        }
    }
}
