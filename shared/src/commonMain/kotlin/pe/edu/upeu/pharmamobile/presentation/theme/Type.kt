package pe.edu.upeu.pharmamobile.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import pharmamobile.shared.generated.resources.Res
import pharmamobile.shared.generated.resources.bricolage_bold
import pharmamobile.shared.generated.resources.bricolage_medium
import pharmamobile.shared.generated.resources.manrope_bold
import pharmamobile.shared.generated.resources.manrope_medium
import pharmamobile.shared.generated.resources.manrope_regular
import pharmamobile.shared.generated.resources.manrope_semibold

@Composable
fun pharmaMobileTypography(): Typography {
    val manrope = FontFamily(
        Font(Res.font.manrope_regular, FontWeight.Normal),
        Font(Res.font.manrope_medium, FontWeight.Medium),
        Font(Res.font.manrope_semibold, FontWeight.SemiBold),
        Font(Res.font.manrope_bold, FontWeight.Bold)
    )
    val bricolage = FontFamily(
        Font(Res.font.bricolage_medium, FontWeight.Medium),
        Font(Res.font.bricolage_bold, FontWeight.Bold)
    )

    return Typography(
        headlineMedium = TextStyle(
            fontFamily = bricolage,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 35.sp,
            letterSpacing = (-1).sp
        ),
        headlineSmall = TextStyle(
            fontFamily = bricolage,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            lineHeight = 30.sp,
            letterSpacing = (-0.6).sp
        ),
        titleLarge = TextStyle(
            fontFamily = manrope,
            fontWeight = FontWeight.Bold,
            fontSize = 19.sp,
            lineHeight = 24.sp,
            letterSpacing = (-0.2).sp
        ),
        titleMedium = TextStyle(
            fontFamily = manrope,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            lineHeight = 21.sp,
            letterSpacing = (-0.1).sp
        ),
        bodyLarge = TextStyle(
            fontFamily = manrope,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            letterSpacing = 0.1.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = manrope,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            letterSpacing = 0.1.sp
        ),
        labelLarge = TextStyle(
            fontFamily = manrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            letterSpacing = 0.1.sp
        ),
        labelSmall = TextStyle(
            fontFamily = manrope,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            lineHeight = 15.sp,
            letterSpacing = 1.2.sp
        )
    )
}
