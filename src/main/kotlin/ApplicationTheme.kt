import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun ApplicationTheme(content: @Composable () -> Unit) =
    DesktopMaterialTheme(
        colors = darkColors(
            background = Color(0xFF2B2B2B),
            surface = Color(0xFF3C3F41),
            primary = Color(0xFF4287F5),
            secondary = Color(0xFF8DF542),
        ),
        content = content
    )
