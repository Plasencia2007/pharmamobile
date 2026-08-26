package pe.edu.upeu.pharmamobile

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import pe.edu.upeu.pharmamobile.presentation.producto.ProductoScreen

@Composable
@Preview
fun App() {
    MaterialTheme {
        ProductoScreen()
    }
}