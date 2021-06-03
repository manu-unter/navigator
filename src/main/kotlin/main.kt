import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    // Change look and feel of the underlying Java/Swing menu on Windows/Linux
    System.setProperty("skiko.rendering.laf.global", "true")

    Window(title = "Navigator", icon = getWindowIcon()) {
        val aboutDialogState = rememberDialogState(isOpen = false, size = WindowSize(400.dp, 250.dp))

        MenuBar {
            Menu("Help") {
                Item("About") { aboutDialogState.isOpen = true }
            }
        }

        ApplicationTheme {
            Navigator()
            AboutDialog(aboutDialogState)
        }
    }
}

private fun getWindowIcon(): BufferedImage? {
    return try {
        ImageIO.read(File("src/main/resources/images/application-icon-96.png"))
    } catch (exception: Exception) {
        null
    }
}
