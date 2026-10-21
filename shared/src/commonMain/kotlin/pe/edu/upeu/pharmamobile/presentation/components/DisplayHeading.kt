package pe.edu.upeu.pharmamobile.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

/** Título de pantalla en Schibsted Grotesk; las dos partes se leen como una sola frase. */
@Composable
fun DisplayHeading(linea1: String, linea2: String) {
    Text(
        text = "$linea1 $linea2",
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}
