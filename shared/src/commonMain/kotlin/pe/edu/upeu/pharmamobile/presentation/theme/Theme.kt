package pe.edu.upeu.pharmamobile.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Emerald,
    onPrimary = Color.White,
    primaryContainer = Mint,
    onPrimaryContainer = EmeraldDeep,
    secondary = Violet,
    onSecondary = Color.White,
    secondaryContainer = VioletSoft,
    onSecondaryContainer = Violet,
    tertiary = Coral,
    onTertiary = Color.White,
    tertiaryContainer = CoralSoft,
    onTertiaryContainer = Coral,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = Sand,
    onSurfaceVariant = Muted,
    outline = Line,
    surfaceContainerHighest = Field,
    error = ErrorLight,
    onError = OnErrorLight
)

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = EmeraldContainerDark,
    onPrimaryContainer = Mint,
    secondary = VioletDark,
    onSecondary = OnPrimaryDark,
    secondaryContainer = VioletContainerDark,
    onSecondaryContainer = VioletSoft,
    tertiary = CoralDark,
    onTertiary = OnPrimaryDark,
    tertiaryContainer = CoralDark.copy(alpha = 0.22f),
    onTertiaryContainer = CoralDark,
    background = PaperDark,
    onBackground = InkDark,
    surface = PaperDark,
    onSurface = InkDark,
    surfaceVariant = SandDark,
    onSurfaceVariant = MutedDark,
    outline = LineDark,
    surfaceContainerHighest = FieldDark,
    error = ErrorDark,
    onError = OnErrorDark
)

@Composable
fun PharmaMobileTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = pharmaMobileTypography(),
        content = content
    )
}
