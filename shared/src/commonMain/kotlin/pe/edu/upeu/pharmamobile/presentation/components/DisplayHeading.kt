package pe.edu.upeu.pharmamobile.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle

@Composable
fun DisplayHeading(linea1: String, linea2: String) {
    val texto = buildAnnotatedString {
        append(linea1 + "\n")
        withStyle(SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
            append(linea2)
        }
    }
    Text(text = texto, style = MaterialTheme.typography.headlineSmall)
}
