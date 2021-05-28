import androidx.compose.desktop.Window
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@ExperimentalFoundationApi
fun main() = Window {
    var path by remember { mutableStateOf("C:\\Users\\mane\\Downloads") }

    val scrollState = rememberScrollState()
    val scrollbarAdapter = rememberScrollbarAdapter(scrollState)

    val selectionState = remember { mutableStateOf<Node?>(null) }

    MaterialTheme {
        Column {
            TextField(value = path, onValueChange = { path = it })
            Box {
                Column(Modifier.verticalScroll(scrollState)) {
                    NodeEntry(FileSystemNode(File(path)), selectionState)
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
            // Some directories can't be listed and will return null
            file.listFiles()?.let { arrayOfFiles ->
                arrayOfFiles.sortWith(comparator = compareBy({ !it.isDirectory }, { it.name }))
                arrayOfFiles.map { FileSystemNode(it) }
            }
                ?: emptyList()
        } else {
            emptyList()
        }
    }
}

val ICON_SIZE = 24.dp

@ExperimentalFoundationApi
@Composable
fun NodeEntry(node: Node, selectionState: MutableState<Node?>, indentation: Int = 0, modifier: Modifier = Modifier) {
    var isExpanded by remember { mutableStateOf(false) }
    val isSelected = selectionState.value === node
    val children by produceState<List<Node>?>(null, node) {
        withContext(Dispatchers.IO) {
            // Done asynchronously to avoid freezes with lots of files in a directory
            value = node.listChildren()
        }
    }

    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth()
                .background(color = if (isSelected) MaterialTheme.colors.secondary else Color.Transparent)
                .selectable(
                    selected = isSelected,
                    onClick = {/* overwritten below */}
                )
                .sequentiallyDoubleClickable(
                    onClick = { selectionState.value = node },
                    onDoubleClick = { isExpanded = !isExpanded },
                )
        ) {
            Spacer(Modifier.width(ICON_SIZE * indentation))
            if (children != null && children!!.count() > 0) {
                if (isExpanded) {
                    NodeIcon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        Modifier.clickable { isExpanded = false }
                    )
                } else {
                    NodeIcon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = "Expand",
                        Modifier.clickable { isExpanded = true }
                    )
                }
            } else {
                Spacer(Modifier.size(ICON_SIZE))
            }
            Text(node.label, color = if (isSelected) MaterialTheme.colors.onSecondary else Color.Unspecified)
        }
        if (isExpanded) {
            children?.forEach {
                NodeEntry(it, selectionState, indentation = indentation + 1)
            }
        }
    }
}

/**
 * Makes the component both single- and double-clickable by *immediately* calling `onClick` on the first click and
 * *then* `onDoubleClick` when a second click comes in. Use this modifier to avoid the delay that the standard
 * `Modifier.combinedClickable` introduces when both an `onClick` and an `onDoubleClick` handler are given.
 *
 * We need to handle the double clicks in userland until the issue has been addressed in the Compose standard library.
 *
 * See https://github.com/JetBrains/compose-jb/issues/255 and https://issuetracker.google.com/issues/177929160
 */
@Composable
fun Modifier.sequentiallyDoubleClickable(onClick: () -> Unit, onDoubleClick: () -> Unit): Modifier {
    val doubleTapMinTimeMillis = LocalViewConfiguration.current.doubleTapMinTimeMillis
    val doubleTapTimeoutMillis = LocalViewConfiguration.current.doubleTapTimeoutMillis
    var lastClickTimeMillis: Long? by remember { mutableStateOf(null) }

    return clickable {
        val isDoubleClick =
            lastClickTimeMillis?.let {
                val millisSinceLastClick = System.currentTimeMillis() - it
                millisSinceLastClick in doubleTapMinTimeMillis until doubleTapTimeoutMillis
            } ?: false

        if (isDoubleClick) {
            lastClickTimeMillis = null
            onDoubleClick()
        } else {
            lastClickTimeMillis = System.currentTimeMillis()
            onClick()
        }
    }
}

@Composable
fun NodeIcon(imageVector: ImageVector, contentDescription: String, modifier: Modifier = Modifier) {
    Icon(imageVector, contentDescription, modifier.size(ICON_SIZE))
}