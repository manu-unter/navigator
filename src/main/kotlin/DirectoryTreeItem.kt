import androidx.compose.foundation.BoxWithTooltip
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.ContentReadable
import model.Expandable
import model.Openable

@Composable
fun DirectoryTreeItem(
    viewNode: ViewNode,
    isSelected: Boolean,
    onSelect: () -> Unit,
    isFocused: Boolean,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(viewNode) {
        withContext(Dispatchers.IO) {
            viewNode.initChildren()
        }
    }

    Surface(
        color =
        if (isSelected)
            if (isFocused) LocalTextSelectionColors.current.backgroundColor
            else LocalContentColor.current.copy(alpha = 0.12f)
        else Color.Transparent,
        modifier = modifier
    ) {
        var wasLabelTruncated: Boolean by remember { mutableStateOf(false) }
        BoxWithTooltip(tooltip = {
            if (wasLabelTruncated) Surface {
                Text(
                    viewNode.node.label,
                    Modifier.padding(12.dp)
                )
            }
        }) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
                    .selectable(
                        selected = isSelected,
                        onClick = {/* overwritten below */ }
                    )
                    .sequentiallyDoubleClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            onSelect()
                        },
                        onDoubleClick = {
                            if (viewNode.node is Expandable) {
                                viewNode.isExpanded = !viewNode.isExpanded
                            } else if (viewNode.node is Openable) {
                                viewNode.node.open()
                            }
                        },
                    )
            ) {
                Spacer(Modifier.width(ICON_SIZE * viewNode.level))
                NodeIcon(viewNode)
                Text(
                    viewNode.node.label,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    onTextLayout = { wasLabelTruncated = it.isLineEllipsized(0) }
                )
            }
        }
    }
}

val ICON_SIZE = 24.dp

@Composable
private fun NodeIcon(viewNode: ViewNode) {
    val modifier = Modifier.size(ICON_SIZE)
    if (viewNode.hasChildren()) {
        if (viewNode.isExpanded) {
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = "Collapse icon",
                modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { viewNode.isExpanded = false }
            )
        } else {
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = "Expand icon",
                modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { viewNode.isExpanded = true }
            )
        }
    } else if (viewNode.node is ContentReadable) {
        val contentDescription = "${viewNode.node.contentType} file icon"
        when (viewNode.node.contentType.split("/")[0]) {
            "image" -> Icon(Icons.Default.Image, contentDescription, modifier, tint = Color(0xFFb4d986))
            "text" -> Icon(Icons.Default.Description, contentDescription, modifier, tint = Color(0xFF7BC7E0))
            "video" -> Icon(Icons.Default.Movie, contentDescription, modifier, tint = Color(0xFFF5855D))
            "application" -> Icon(Icons.Default.InsertDriveFile, contentDescription, modifier, tint = Color(0xFFddb6f0))
            else -> Icon(Icons.Default.InsertDriveFile, contentDescription, modifier)
        }
    } else {
        Spacer(modifier)
    }
}
