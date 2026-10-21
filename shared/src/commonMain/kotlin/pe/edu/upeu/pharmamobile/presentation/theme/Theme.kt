package pe.edu.upeu.pharmamobile.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Tinta,
    onPrimary = Blanco,
    primaryContainer = LineaSuave,
    onPrimaryContainer = Tinta,
    secondary = Coral,
    onSecondary = Blanco,
    secondaryContainer = CoralTinte,
    onSecondaryContainer = RojoTexto,
    tertiary = Turquesa,
    onTertiary = Blanco,
    background = Blanco,
    onBackground = Tinta,
    surface = Blanco,
    onSurface = Tinta,
    surfaceVariant = Superficie,
    onSurfaceVariant = Texto3,
    surfaceContainerHighest = LineaSuave,
    outline = Borde,
    outlineVariant = Linea,
    error = RojoAccion,
    onError = Blanco
)

private val DarkColorScheme = darkColorScheme(
    primary = TextoOscuro,
    onPrimary = FondoOscuro,
    primaryContainer = CampoOscuro,
    onPrimaryContainer = TextoOscuro,
    secondary = Coral,
    onSecondary = Blanco,
    secondaryContainer = Coral.copy(alpha = 0.22f),
    onSecondaryContainer = CoralTinte,
    tertiary = Turquesa,
    onTertiary = Blanco,
    background = FondoOscuro,
    onBackground = TextoOscuro,
    surface = FondoOscuro,
    onSurface = TextoOscuro,
    surfaceVariant = SuperficieOscura,
    onSurfaceVariant = Texto3Oscuro,
    surfaceContainerHighest = CampoOscuro,
    outline = LineaOscura,
    outlineVariant = LineaOscura,
    error = RojoBorde,
    onError = FondoOscuro
)

@Composable
fun PharmaMobileTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = pharmaMobileTypography(),
        content = content
    )
}
