package app.krafted.tigerluckysnap.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val TigerColorScheme = darkColorScheme(
    primary              = TigerGold,
    onPrimary            = TigerBrown,
    primaryContainer     = TigerBrownMid,
    onPrimaryContainer   = TigerCream,
    secondary            = TigerGoldDeep,
    onSecondary          = TigerBrown,
    secondaryContainer   = TigerBrownLight,
    onSecondaryContainer = TigerCream,
    background           = TigerBrown,
    onBackground         = TigerCream,
    surface              = TigerBrownMid,
    onSurface            = TigerCream,
    surfaceVariant       = TigerBrownLight,
    onSurfaceVariant     = TigerGold,
    outline              = TigerGoldDeep,
    error                = TigerRed,
    onError              = TigerCream,
)

@Composable
fun TigerLuckySnapTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TigerColorScheme,
        typography  = Typography,
        content     = content
    )
}
