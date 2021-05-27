import androidx.compose.desktop.Window
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import java.io.File

fun main() = Window {
    var path by remember { mutableStateOf("C:\\Users\\mane\\Downloads") }

    val scrollState = rememberScrollState()
    val scrollbarAdapter = rememberScrollbarAdapter(scrollState)

    MaterialTheme {
        Column {
            TextField(path, onValueChange = {
                path = it
            })
            Box {
                Column(Modifier.verticalScroll(scrollState)) {
                    NodeEntry(FileSystemNode(File(path)))
                }
                VerticalScrollbar(scrollbarAdapter, Modifier.align(Alignment.CenterEnd))
            }
        }
    }
}

interface Node {
    val label: String
    fun listChildren(): List<Node>
}

class FileSystemNode(private val file: File) : Node {
    override val label: String get() = file.name
    override fun listChildren(): List<Node> {
        return if (file.isDirectory) {
            file.listFiles()?.map { FileSystemNode(it) } ?: emptyList()
        } else {
            emptyList()
        }
    }
}

@Composable
fun NodeEntry(node: Node, modifier: Modifier = Modifier) {
    var isExpanded by remember { mutableStateOf(false) }
    val children by produceState<List<Node>?>(null, node) {
        // Done asynchronously to avoid freezes with lots of files in a directory
        value = node.listChildren()
    }

    Column(modifier) {
        Row {
            if (children == null) {
                Text("...")
            } else if (children != null && children!!.count() > 0) {
                Button(onClick = { isExpanded = !isExpanded }) {
                    if (isExpanded) {
                        Text("-")
                    } else {
                        Text("+")
                    }
                }
            } else {
                Text(" ") // TODO Replace with Chevron icons
            }
            Text(node.label)
        }
        Divider()
        if (isExpanded) {
            children?.forEach {
                NodeEntry(it)
            }
        }
    }
}