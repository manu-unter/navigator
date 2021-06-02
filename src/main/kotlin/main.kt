import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import model.Node
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

        var rootPath by remember {
            mutableStateOf(
                System.getProperty("user.home") ?: System.getenv("SystemDrive") ?: "/"
            )
        }
        val rootViewNode by derivedStateOf { ViewNode(Node(File(rootPath))) }
        val selectionState = remember { mutableStateOf<ViewNode?>(rootViewNode) }

        ApplicationTheme {
            Row(Modifier.fillMaxSize().background(color = MaterialTheme.colors.background)) {

                Surface(Modifier.weight(1f)) {
                    Column {
                        TextField(
                            value = rootPath,
                            onValueChange = { rootPath = it },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(2.dp))
                        DirectoryTree(rootViewNode, selectionState)
                    }
                }
                Preview(
                    selectionState.value?.node,
                    modifier = Modifier.fillMaxHeight().weight(2f)
                )
            }

            AboutDialog(aboutDialogState)
        }
    }
}

fun getWindowIcon(): BufferedImage? {
    return try {
        ImageIO.read(File("src/main/resources/images/application-icon-96.png"))
    } catch (exception: Exception) {
        null
    }
}