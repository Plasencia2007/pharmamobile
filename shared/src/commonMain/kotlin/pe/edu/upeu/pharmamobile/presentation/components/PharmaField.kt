package pe.edu.upeu.pharmamobile.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun PharmaField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    errorMessage: String? = null,
    optional: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val colores = MaterialTheme.colorScheme

    val borderColor = when {
        errorMessage != null -> colores.error
        isFocused -> colores.secondary
        else -> colores.outline
    }
    val fieldBg = if (isFocused) colores.background else colores.surfaceContainerHighest

    Column(modifier = modifier.fillMaxWidth()) {
        Row {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = colores.onSurfaceVariant
            )
            if (optional) {
                Text(
                    text = "  opcional",
                    style = MaterialTheme.typography.labelSmall,
                    color = colores.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .fillMaxWidth()
                .height(52.dp)
                .background(fieldBg, RoundedCornerShape(14.dp))
                .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colores.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = colores.onSurface),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(colores.secondary),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                interactionSource = interactionSource,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (errorMessage != null) {
            Row(
                modifier = Modifier.padding(top = 5.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = colores.error,
                    modifier = Modifier.height(14.dp)
                )
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.labelLarge,
                    color = colores.error,
                    modifier = Modifier.padding(start = 5.dp)
                )
            }
        }
    }
}
