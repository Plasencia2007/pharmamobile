package pe.edu.upeu.pharmamobile.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Pine,
    onPrimary = Paper,
    primaryContainer = Mint,
    onPrimaryContainer = Pine,
    secondary = Pine2,
    onSecondary = Paper,
    secondaryContainer = Sand,
    onSecondaryContainer = Ink,
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
    primary = PineDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PineContainerDark,
    onPrimaryContainer = Mint,
    secondary = PineDark,
    onSecondary = OnPrimaryDark,
    secondaryContainer = SandDark,
    onSecondaryContainer = InkDark,
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
