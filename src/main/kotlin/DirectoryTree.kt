import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.shortcuts
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.Expandable
import model.Node

@ExperimentalAnimationApi
@Composable
fun DirectoryTree(
    rootNode: Node,
    selectionState: MutableState<Node?> = remember { mutableStateOf<Node?>(null) },
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val scrollbarAdapter = rememberScrollbarAdapter(scrollState)
    val mutableInteractionSource = remember { MutableInteractionSource() }
    val focusRequester = FocusRequester()

    val isFocused by mutableInteractionSource.collectIsFocusedAsState()

    val borderWidth by animateDpAsState(
        if (isFocused) 2.dp else 1.dp,
        tween(durationMillis = 150)
    )

    Box(
        modifier = modifier
            .focusRequester(focusRequester)
            .focusable(interactionSource = mutableInteractionSource)
            .then(
                if (isFocused)
                    Modifier.border(
                        width = borderWidth,
                        color = MaterialTheme.colors.primary,
                        shape = MaterialTheme.shapes.large,
                    )
                else
                    Modifier
            )
            .clickable { focusRequester.requestFocus() }
            .shortcuts {
                on(Key.Escape) { selectionState.value = null }
            }) {
        Column(Modifier.verticalScroll(scrollState)) {
            NodeEntry(rootNode, selectionState, isFocused, focusRequester)
        }
        VerticalScrollbar(scrollbarAdapter, Modifier.align(Alignment.CenterEnd))
    }
}

val ICON_SIZE = 24.dp

@ExperimentalAnimationApi
@Composable
private fun NodeEntry(
    node: Node,
    selectionState: MutableState<Node?>,
    isFocused: Boolean,
    focusRequester: FocusRequester,
    indentation: Int = 0,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val isSelected = selectionState.value === node
    val children by produceState<List<Node>?>(initialValue = null, node) {
        if (node is Expandable) {
            withContext(Dispatchers.IO) {
                // Done asynchronously to avoid freezes with lots of files in a directory
                value = node.listChildren()
            }
        }
    }

    Column(modifier.fillMaxWidth()) {
        Surface(
            color =
            if (isSelected)
                if (isFocused) LocalTextSelectionColors.current.backgroundColor
                else LocalContentColor.current.copy(alpha = 0.12f)
            else Color.Transparent
        ) {
            Row(
                Modifier.fillMaxWidth()
                    .selectable(
                        selected = isSelected,
                        onClick = {/* overwritten below */ }
                    )
                    .sequentiallyDoubleClickable(
                        onClick = {
                            focusRequester.requestFocus()
                            selectionState.value = node
                        },
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
                )
            }
        }
        children?.forEach {
            AnimatedVisibility(
                isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                NodeEntry(it, selectionState, isFocused, focusRequester, indentation = indentation + 1)
            }
        }
    }
}

@Composable
private fun NodeIcon(imageVector: ImageVector, contentDescription: String, modifier: Modifier = Modifier) {
    Icon(
        imageVector,
        contentDescription,
        modifier = modifier.size(ICON_SIZE)
    )
}
