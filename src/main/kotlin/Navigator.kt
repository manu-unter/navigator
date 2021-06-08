import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import model.Node
import java.io.File

@Composable
fun Navigator() {
    var rootPath by remember { mutableStateOf(getInitialRootPath()) }
    val rootViewNode by remember { derivedStateOf { getValidRootViewNode(rootPath) } }
    val selectionState: MutableState<ViewNode?> = remember { mutableStateOf(rootViewNode) }

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
                    DirectoryTree(
                        rootViewNode!!,
                        selectionState,
                    )
                } else {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Box(modifier = Modifier.height(24.dp), contentAlignment = Alignment.CenterStart) {
                            Text("Please provide a valid path", Modifier.padding(24.dp, 0.dp))
                        }
                    }
                }
            }
        }
        Preview(
            selectionState.value?.node,
            modifier = Modifier.fillMaxHeight().weight(2f)
        )
    }

}

private fun getInitialRootPath(): String {
    return System.getProperty("user.home") ?: System.getenv("SystemDrive") ?: "/"
}

private fun getValidRootViewNode(path: String): ViewNode? {
    val file = File(path)

    if (!file.exists()) {
        return null
    }

    return ViewNode(Node(file.canonicalFile))
}
