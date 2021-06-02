import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
        Row(
            Modifier.fillMaxWidth()
                .selectable(
                    selected = isSelected,
                    onClick = {/* overwritten below */ }
                )
                .sequentiallyDoubleClickable(
                    onClick = {
                        onSelect()
                    },
                    onDoubleClick = {
                        viewNode.isExpanded = !viewNode.isExpanded
                    },
                )
        ) {
            Spacer(Modifier.width(ICON_SIZE * viewNode.level))
            if (viewNode.hasChildren()) {
                if (viewNode.isExpanded) {
                    NodeIcon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse icon",
                        Modifier.clickable { viewNode.isExpanded = false }
                    )
                } else {
                    NodeIcon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = "Expand icon",
                        Modifier.clickable { viewNode.isExpanded = true }
                    )
                }
            } else {
                Spacer(Modifier.size(ICON_SIZE))
            }
            Text(
                viewNode.node.label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

val ICON_SIZE = 24.dp

@Composable
private fun NodeIcon(imageVector: ImageVector, contentDescription: String, modifier: Modifier = Modifier) {
    Icon(
        imageVector,
        contentDescription,
        modifier = modifier.size(ICON_SIZE)
    )
}
