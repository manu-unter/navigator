import androidx.compose.desktop.DesktopTheme
import androidx.compose.desktop.Window
import androidx.compose.foundation.layout.*
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import model.Node
import java.io.File

fun main() = Window {
    var rootPath by remember { mutableStateOf("C:\\Users\\mane\\Downloads") }
    val rootNode by derivedStateOf { Node(File(rootPath)) }
    val selectionState = remember { mutableStateOf<Node?>(null) }

    DesktopTheme {
        Row(Modifier.fillMaxSize()) {

            Column(Modifier.weight(1f)) {
                TextField(
                    value = rootPath,
                    onValueChange = { rootPath = it },
                    modifier = Modifier.fillMaxWidth()
                )
                DirectoryTree(rootNode, selectionState)
            }
            Preview(
                selectionState.value,
                modifier = Modifier.fillMaxHeight().weight(2f)
            )
        }
    }
}