import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
    rootViewNode: ViewNode,
    selectionState: MutableState<ViewNode?> = remember { mutableStateOf<ViewNode?>(null) },
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()
    val scrollbarAdapter = rememberScrollbarAdapter(lazyListState)
    val mutableInteractionSource = remember { MutableInteractionSource() }
    val focusRequester = FocusRequester()

    val isFocused by mutableInteractionSource.collectIsFocusedAsState()

    val borderWidth by animateDpAsState(
        if (isFocused) 2.dp else 1.dp,
        tween(durationMillis = 150)
    )

    val listOfViewNodes by remember(rootViewNode) { derivedStateOf { rootViewNode.listExpandedNodesDepthFirst() } }

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
                on(Key.DirectionUp) {
                    val currentSelectionIndex = listOfViewNodes.indexOf(selectionState.value)
                    if (currentSelectionIndex > 0) {
                        selectionState.value = listOfViewNodes.get(listOfViewNodes.indexOf(selectionState.value) - 1)
                    }
                }
                on(Key.DirectionDown) {
                    val currentSelectionIndex = listOfViewNodes.indexOf(selectionState.value)
                    if (currentSelectionIndex < listOfViewNodes.size - 1) {
                        selectionState.value = listOfViewNodes.get(listOfViewNodes.indexOf(selectionState.value) + 1)
                    }
                }
                on(Key.DirectionLeft) {
                    selectionState.value?.parent?.let {
                        selectionState.value = it
                        it.isExpanded = false
                    }
                }
                on(Key.DirectionRight) {
                    selectionState.value?.firstChild?.let {
                        selectionState.value!!.isExpanded = true
                        selectionState.value = it

                    }
                }
            }) {

        LazyColumn(state = lazyListState) {
            items(count = listOfViewNodes.size) { index ->
                val viewNode = listOfViewNodes[index]
                NodeEntry(
                    viewNode,
                    isSelected = selectionState.value === viewNode,
                    onSelect = {
                        focusRequester.requestFocus()
                        selectionState.value = viewNode
                    },
                    isFocused = isFocused
                )
            }
        }
        VerticalScrollbar(scrollbarAdapter, Modifier.align(Alignment.CenterEnd))
    }

}

/**
 * View-oriented data structure which enables several features:
 *  - Remembering if a node is expanded or not, even if it isn't currently listed in the tree
 *  - Lazily reading and remembering directory and archive contents from the file system only for visible nodes
 *  - Returning a depth-first listing of all nodes to flatten the tree into a List<ViewNode>, which facilitates using
 *    it with a LazyColumn
 */
class ViewNode(
    val node: Node,
    val parent: ViewNode? = null,
    val level: Int = 0
) {

    var isExpanded by mutableStateOf(node is Expandable && level == 0)

    private var children by mutableStateOf<List<ViewNode>?>(
        if (node is Expandable) null else emptyList()
    )

    fun hasChildren(): Boolean {
        return children?.isNotEmpty() ?: false
    }

    val firstChild get() = if (children?.isNotEmpty() == true) children?.get(0) else null

    fun initChildren() {
        if (node is Expandable && children == null) {
            val childNodes = node.listChildren()
            children = childNodes.map {
                ViewNode(
                    node = it,
                    parent = this@ViewNode,
                    level = level + 1
                )
            }
        }
    }

    fun listExpandedNodesDepthFirst(): List<ViewNode> {
        val listOfViewNodes = mutableListOf(this)
        if (isExpanded) {
            children?.forEach {
                listOfViewNodes += it.listExpandedNodesDepthFirst()
            }
        }
        return listOfViewNodes
    }
}


val ICON_SIZE = 24.dp

@Composable
private fun NodeEntry(
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
                            contentDescription = "Collapse",
                            Modifier.clickable { viewNode.isExpanded = false }
                        )
                    } else {
                        NodeIcon(
                            Icons.Default.KeyboardArrowRight,
                            contentDescription = "Expand",
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
}

@Composable
private fun NodeIcon(imageVector: ImageVector, contentDescription: String, modifier: Modifier = Modifier) {
    Icon(
        imageVector,
        contentDescription,
        modifier = modifier.size(ICON_SIZE)
    )
}
