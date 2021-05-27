import androidx.compose.desktop.Window
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
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

val ICON_SIZE = 24.dp

@Composable
fun NodeEntry(node: Node, indentation: Int = 0, modifier: Modifier = Modifier) {
    var isExpanded by remember { mutableStateOf(false) }
    val children by produceState<List<Node>?>(null, node) {
        // Done asynchronously to avoid freezes with lots of files in a directory
        value = node.listChildren()
    }

    Column(modifier) {
        Row {
            Spacer(Modifier.width(ICON_SIZE * indentation))
            if (children != null && children!!.count() > 0) {
                if (isExpanded) {
                    NodeIcon(Icons.Default.KeyboardArrowDown, "Collapse", Modifier.clickable { isExpanded = false })
                } else {
                    NodeIcon(Icons.Default.KeyboardArrowRight, "Expand", Modifier.clickable { isExpanded = true })
                }
            } else {
                Spacer(Modifier.size(ICON_SIZE))
            }
            Text(node.label)
        }
        if (isExpanded) {
            children?.forEach {
                NodeEntry(it, indentation = indentation + 1)
            }
        }
    }
}

@Composable
fun NodeIcon(imageVector: ImageVector, contentDescription: String, modifier: Modifier = Modifier) {
    Icon(imageVector, contentDescription, modifier.size(ICON_SIZE))
}