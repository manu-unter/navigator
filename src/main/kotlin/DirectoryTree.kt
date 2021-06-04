import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.shortcuts
import androidx.compose.ui.unit.dp

@Composable
fun DirectoryTree(
    rootViewNode: ViewNode,
    selectedViewNode: ViewNode?,
    onSelect: (ViewNode?) -> Unit,
    modifier: Modifier = Modifier
) {
    val listOfViewNodes by remember {
        derivedStateOf {
            val list = mutableListOf<ViewNode>()
            rootViewNode.addVisibleViewNodesDepthFirst(list)
            list
        }
    }

    val lazyListState = rememberLazyListState()
    val scrollbarAdapter = rememberScrollbarAdapter(lazyListState)
    val mutableInteractionSource = remember { MutableInteractionSource() }
    val focusRequester = remember { FocusRequester() }

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
            .clickable(
                interactionSource = mutableInteractionSource,
                indication = null
            ) { focusRequester.requestFocus() }
            .shortcuts {
                on(Key.Escape) { onSelect(null) }
                on(Key.DirectionUp) {
                    val currentSelectionIndex = listOfViewNodes.indexOf(selectedViewNode)
                    if (currentSelectionIndex > 0) {
                        onSelect(listOfViewNodes[currentSelectionIndex - 1])
                    }
                }
                on(Key.DirectionDown) {
                    val currentSelectionIndex = listOfViewNodes.indexOf(selectedViewNode)
                    if (currentSelectionIndex < listOfViewNodes.size - 1) {
                        onSelect(listOfViewNodes[currentSelectionIndex + 1])
                    }
                }
                on(Key.DirectionLeft) {
                    selectedViewNode?.parent?.let {
                        onSelect(it)
                        it.isExpanded = false
                    }
                }
                on(Key.DirectionRight) {
                    selectedViewNode?.firstChild?.let {
                        selectedViewNode.isExpanded = true
                        onSelect(it)
                    }
                }
            }) {

        LazyColumn(state = lazyListState) {
            items(count = listOfViewNodes.size) { index ->
                val viewNode = listOfViewNodes[index]
                DirectoryTreeItem(
                    viewNode,
                    isSelected = selectedViewNode === viewNode,
                    onSelect = {
                        focusRequester.requestFocus()
                        onSelect(viewNode)
                    },
                    isFocused = isFocused
                )
            }
        }
        VerticalScrollbar(scrollbarAdapter, Modifier.align(Alignment.CenterEnd))
    }

}
