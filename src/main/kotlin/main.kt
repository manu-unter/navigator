import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
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

        var rootPath by remember { mutableStateOf(getInitialRootPath()) }
        val rootViewNode by remember { derivedStateOf { getValidRootViewNode(rootPath) } }
        val selectionState = remember { mutableStateOf<ViewNode?>(rootViewNode) }

        ApplicationTheme {
            Row(Modifier.fillMaxSize().background(color = MaterialTheme.colors.background)) {

                Surface(Modifier.weight(1f)) {
                    Column(Modifier.fillMaxSize()) {
                        TextField(
                            value = rootPath,
                            onValueChange = { rootPath = it },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(2.dp))
                        if (rootViewNode != null) {
                            DirectoryTree(rootViewNode!!, selectionState)
                        } else {
                            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                Text("Please provide a valid path", Modifier.padding(24.dp, 0.dp))
                            }
                        }
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

fun getInitialRootPath(): String {
    return System.getProperty("user.home") ?: System.getenv("SystemDrive") ?: "/"
}

fun getValidRootViewNode(path: String): ViewNode? {
    val file = File(path)

    if (!file.exists()) {
        return null
    }

    return ViewNode(Node(file.canonicalFile))
}