package pe.edu.upeu.pharmamobile.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import pharmamobile.shared.generated.resources.Res
import pharmamobile.shared.generated.resources.atkinson_bold
import pharmamobile.shared.generated.resources.atkinson_mono_regular
import pharmamobile.shared.generated.resources.atkinson_mono_semibold
import pharmamobile.shared.generated.resources.atkinson_regular
import pharmamobile.shared.generated.resources.atkinson_semibold
import pharmamobile.shared.generated.resources.schibsted_bold
import pharmamobile.shared.generated.resources.schibsted_extrabold

/*
 * Atkinson Hyperlegible (Braille Institute) para el texto: distingue letras
 * que se confunden (I l 1, O 0, rn m), clave para leer nombres de
 * medicamentos parecidos. Su versión Mono para precios, stock e ids, y
 * Schibsted Grotesk para los títulos. Todas con licencia OFL
 * (docs/licencias-fuentes); los .ttf son instancias estáticas por peso de
 * las fuentes variables de google/fonts.
 */
@Composable
private fun atkinson() = FontFamily(
    Font(Res.font.atkinson_regular, FontWeight.Normal),
    Font(Res.font.atkinson_semibold, FontWeight.SemiBold),
    Font(Res.font.atkinson_bold, FontWeight.Bold)
)

@Composable
private fun schibsted() = FontFamily(
    Font(Res.font.schibsted_bold, FontWeight.Bold),
    Font(Res.font.schibsted_extrabold, FontWeight.ExtraBold)
)

/** Familia monoespaciada para cifras alineadas (precios, stock, ids, horas). */
@Composable
fun fuenteMono() = FontFamily(
    Font(Res.font.atkinson_mono_regular, FontWeight.Normal),
    Font(Res.font.atkinson_mono_semibold, FontWeight.SemiBold)
)

@Composable
fun pharmaMobileTypography(): Typography {
    val texto = atkinson()
    val titulos = schibsted()
    return Typography(
        displaySmall = TextStyle(fontFamily = titulos, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 36.sp, letterSpacing = (-0.8).sp),
        headlineMedium = TextStyle(fontFamily = titulos, fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-0.7).sp),
        headlineSmall = TextStyle(fontFamily = titulos, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = (-0.4).sp),
        titleLarge = TextStyle(fontFamily = titulos, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 24.sp, letterSpacing = (-0.2).sp),
        titleMedium = TextStyle(fontFamily = texto, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp),
        titleSmall = TextStyle(fontFamily = texto, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 18.sp),
        bodyLarge = TextStyle(fontFamily = texto, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
        bodyMedium = TextStyle(fontFamily = texto, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
        bodySmall = TextStyle(fontFamily = texto, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
        labelLarge = TextStyle(fontFamily = texto, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 18.sp),
        labelMedium = TextStyle(fontFamily = texto, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 16.sp),
        labelSmall = TextStyle(fontFamily = texto, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 15.sp, letterSpacing = 0.3.sp)
    )
}
