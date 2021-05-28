import androidx.compose.desktop.Window
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import java.io.File

fun main() = Window {
    var rootPath by remember { mutableStateOf("C:\\Users\\mane\\Downloads") }
    val rootNode by derivedStateOf { FileSystemNode(File(rootPath)) }
    val selectionState = remember { mutableStateOf<Node?>(null) }

    MaterialTheme {
        Column {
            TextField(value = rootPath, onValueChange = { rootPath = it })
            DirectoryTree(rootNode, selectionState)
        }
    }
}