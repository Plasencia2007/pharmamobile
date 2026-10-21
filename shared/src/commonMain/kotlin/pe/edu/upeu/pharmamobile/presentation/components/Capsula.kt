package pe.edu.upeu.pharmamobile.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pe.edu.upeu.pharmamobile.domain.model.Producto
import pe.edu.upeu.pharmamobile.presentation.theme.Blanco
import pe.edu.upeu.pharmamobile.presentation.theme.Borde
import pe.edu.upeu.pharmamobile.presentation.theme.Coral
import pe.edu.upeu.pharmamobile.presentation.theme.Tinta
import pe.edu.upeu.pharmamobile.presentation.theme.Turquesa

/** Marcador de dos mitades con forma de cápsula: el motivo visual del rediseño. */
@Composable
fun Capsula(
    color: Color,
    modifier: Modifier = Modifier,
    ancho: Dp = 26.dp,
    alto: Dp = 12.dp,
    contorno: Color = Tinta,
    grosor: Dp = 1.5.dp,
    giro: Float = 0f
) {
    val forma = RoundedCornerShape(alto / 2)
    Row(
        modifier = modifier
            .rotate(giro)
            .width(ancho)
            .height(alto)
            .clip(forma)
            .border(grosor, contorno, forma)
    ) {
        Box(Modifier.weight(1f).fillMaxHeight().background(color))
        Box(Modifier.weight(1f).fillMaxHeight().background(Blanco))
    }
}

/** Color de la cápsula según el estado del producto (la app aún no guarda la categoría). */
fun colorCapsula(producto: Producto): Color = when {
    producto.estaInactivo() -> Borde
    producto.requiereReposicion() -> Coral
    else -> Turquesa
}

/** Etiqueta redondeada pequeña ("En venta", "Sincronizado", "Reponer"). */
@Composable
fun Pildora(
    texto: String,
    fondo: Color,
    colorTexto: Color,
    modifier: Modifier = Modifier,
    punto: Color? = null
) {
    Row(
        modifier = modifier
            .height(26.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(fondo)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (punto != null) {
            Box(Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(punto))
            Box(Modifier.width(6.dp))
        }
        Text(texto, style = MaterialTheme.typography.labelMedium, color = colorTexto)
    }
}
