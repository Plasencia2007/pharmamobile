package pe.edu.upeu.pharmamobile.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PharmaChip(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    val colores = MaterialTheme.colorScheme
    val fondo = if (seleccionado) colores.primary else colores.secondaryContainer
    val textoColor = if (seleccionado) colores.onPrimary else colores.onSurface

    Text(
        text = texto,
        style = MaterialTheme.typography.labelLarge,
        color = textoColor,
        modifier = Modifier
            .background(fondo, RoundedCornerShape(999.dp))
            .border(1.dp, if (seleccionado) fondo else colores.outline, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    )
}
