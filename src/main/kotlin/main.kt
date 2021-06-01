import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.desktop.Window
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import model.Node
import java.io.File

@ExperimentalAnimationApi
fun main() = Window(title = "Navigator") {
    var rootPath by remember { mutableStateOf("C:\\Users\\mane\\Downloads") }
    val rootViewNode by derivedStateOf { ViewNode(Node(File(rootPath))) }
    val selectionState = remember { mutableStateOf<ViewNode?>(null) }

    ApplicationTheme {
        Row(Modifier.fillMaxSize().background(color = MaterialTheme.colors.background)) {

            Surface(Modifier.weight(1f)) {
                Column {
                    TextField(
                        value = rootPath,
                        onValueChange = { rootPath = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                    DirectoryTree(rootViewNode, selectionState)
                }
            }
            Preview(
                selectionState.value?.node,
                modifier = Modifier.fillMaxHeight().weight(2f)
            )
        }
    }
}