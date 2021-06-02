import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
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

    val listOfViewNodes by remember(rootViewNode) {
        derivedStateOf {
            val list = mutableListOf<ViewNode>()
            rootViewNode.addExpandedViewNodesDepthFirst(list)
            list
        }
    }

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
                DirectoryTreeItem(
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
