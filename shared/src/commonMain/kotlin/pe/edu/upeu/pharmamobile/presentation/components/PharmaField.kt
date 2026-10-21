package pe.edu.upeu.pharmamobile.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobile.presentation.theme.fuenteMono

/**
 * Campo de formulario: etiqueta arriba, borde fino que se oscurece con el
 * foco y mensaje de error (o ayuda) debajo. Los campos numéricos y los que
 * llevan [prefijo] (por ejemplo "S/") usan la fuente monoespaciada.
 */
@Composable
fun PharmaField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    errorMessage: String? = null,
    optional: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    esClave: Boolean = false,
    modifier: Modifier = Modifier,
    prefijo: String? = null,
    ayuda: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val enfocado by interactionSource.collectIsFocusedAsState()
    val colores = MaterialTheme.colorScheme
    val numerico = keyboardType == KeyboardType.Decimal || keyboardType == KeyboardType.Number || prefijo != null
    val mono = fuenteMono()
    val estiloTexto: TextStyle = if (numerico) {
        MaterialTheme.typography.bodyLarge.copy(fontFamily = mono, color = colores.onSurface)
    } else {
        MaterialTheme.typography.bodyLarge.copy(color = colores.onSurface)
    }
    val forma = RoundedCornerShape(12.dp)
    val colorBorde = when {
        errorMessage != null -> colores.error
        enfocado -> colores.primary
        else -> colores.outline
    }
    val grosor = if (errorMessage != null || enfocado) 1.5.dp else 1.dp

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = colores.onSurfaceVariant)
            if (optional) {
                Text("  opcional", style = MaterialTheme.typography.bodySmall, color = colores.onSurfaceVariant.copy(alpha = 0.7f))
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(forma)
                .background(colores.background)
                .border(grosor, colorBorde, forma),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (prefijo != null) {
                Box(
                    modifier = Modifier.fillMaxHeight().background(colores.surfaceVariant).padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(prefijo, style = MaterialTheme.typography.bodyMedium.copy(fontFamily = mono), color = colores.onSurfaceVariant)
                }
            }
            Box(modifier = Modifier.weight(1f).padding(horizontal = 14.dp), contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(placeholder, style = estiloTexto, color = colores.onSurfaceVariant.copy(alpha = 0.6f))
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = estiloTexto,
                    cursorBrush = SolidColor(colores.primary),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    visualTransformation = if (esClave) PasswordVisualTransformation() else VisualTransformation.None,
                    interactionSource = interactionSource,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        when {
            errorMessage != null -> Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = colores.error, modifier = Modifier.height(14.dp))
                Text(errorMessage, style = MaterialTheme.typography.bodySmall, color = colores.error, modifier = Modifier.padding(start = 5.dp))
            }
            ayuda != null -> Text(ayuda, style = MaterialTheme.typography.bodySmall, color = colores.onSurfaceVariant)
        }
    }
}
