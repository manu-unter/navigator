import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.Node

@Composable
fun DirectoryTree(
    rootNode: Node,
    selectionState: MutableState<Node?> = remember { mutableStateOf<Node?>(null) },
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val scrollbarAdapter = rememberScrollbarAdapter(scrollState)

    Box(modifier = modifier) {
        Column(Modifier.verticalScroll(scrollState)) {
            NodeEntry(rootNode, selectionState)
        }
        VerticalScrollbar(scrollbarAdapter, Modifier.align(Alignment.CenterEnd))
    }
}

val ICON_SIZE = 24.dp

@Composable
fun NodeEntry(node: Node, selectionState: MutableState<Node?>, indentation: Int = 0, modifier: Modifier = Modifier) {
    var isExpanded by remember { mutableStateOf(false) }
    val isSelected = selectionState.value === node
    val children by produceState<List<Node>?>(initialValue = null, node) {
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
            Text(
                node.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isSelected) MaterialTheme.colors.onSecondary else Color.Unspecified
            )
        }
        if (isExpanded) {
            children?.forEach {
                NodeEntry(it, selectionState, indentation = indentation + 1)
            }
        }
    }
}

@Composable
fun NodeIcon(imageVector: ImageVector, contentDescription: String, modifier: Modifier = Modifier) {
    Icon(imageVector, contentDescription, modifier.size(ICON_SIZE))
}
