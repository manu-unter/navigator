import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
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
    selectionState: MutableState<ViewNode?>,
    modifier: Modifier = Modifier
) {
    val listOfViewNodes by remember {
        derivedStateOf {
            val list = mutableListOf<ViewNode>()
            rootViewNode.addVisibleViewNodesDepthFirst(list)
            list
        }
    }
    var selectedViewNode by selectionState

    val lazyListState = rememberLazyListState()

    LaunchedEffect(listOfViewNodes, selectedViewNode) {
        val selectedItemIndex = listOfViewNodes.indexOf(selectedViewNode)
        scrollSelectedViewNodeIntoViewportIfNecessary(lazyListState, selectedItemIndex)
    }

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
                on(Key.Escape) { selectedViewNode = null }
                on(Key.DirectionUp) {
                    val currentSelectionIndex = listOfViewNodes.indexOf(selectedViewNode)
                    if (currentSelectionIndex > 0) {
                        selectedViewNode = listOfViewNodes[currentSelectionIndex - 1]
                    }
                }
                on(Key.DirectionDown) {
                    val currentSelectionIndex = listOfViewNodes.indexOf(selectedViewNode)
                    if (currentSelectionIndex < listOfViewNodes.size - 1) {
                        selectedViewNode = listOfViewNodes[currentSelectionIndex + 1]
                    }
                }
                on(Key.DirectionLeft) {
                    selectedViewNode?.parent?.let {
                        selectedViewNode = it
                        it.isExpanded = false
                    }
                }
                on(Key.DirectionRight) {
                    selectedViewNode?.firstChild?.let {
                        selectedViewNode!!.isExpanded = true
                        selectedViewNode = it
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
                        selectedViewNode = viewNode
                    },
                    isFocused = isFocused
                )
            }
        }
        VerticalScrollbar(scrollbarAdapter, Modifier.align(Alignment.CenterEnd))
    }
}

/**
 * Triggers animateScrollIntoView() with the appropriate parameters when necessary to always keep the selected item
 * fully visible in the viewport.
 * This function assumes that all items have the same height!
 */
suspend fun scrollSelectedViewNodeIntoViewportIfNecessary(lazyListState: LazyListState, selectedItemIndex: Int) {
    if (lazyListState.layoutInfo.visibleItemsInfo.isEmpty() || selectedItemIndex == -1) {
        return
    }

    val firstFullyVisibleItemIndex =
        if (lazyListState.firstVisibleItemScrollOffset == 0) {
            lazyListState.firstVisibleItemIndex
        } else {
            lazyListState.firstVisibleItemIndex + 1
        }
    val itemSize = lazyListState.layoutInfo.visibleItemsInfo[0].size
    val viewportSize = lazyListState.layoutInfo.viewportEndOffset - lazyListState.layoutInfo.viewportStartOffset
    val maximumFullyVisibleItemCount = viewportSize / itemSize
    val lastFullyVisibleItemIndex =
        if (lazyListState.layoutInfo.visibleItemsInfo.last().offset + itemSize > lazyListState.layoutInfo.viewportEndOffset) {
            lazyListState.layoutInfo.visibleItemsInfo.last().index - 1
        } else {
            lazyListState.layoutInfo.visibleItemsInfo.last().index
        }

    val cutOffItemHeight = itemSize - (viewportSize % itemSize)

    if (selectedItemIndex < firstFullyVisibleItemIndex) {
        lazyListState.animateScrollToItem(selectedItemIndex)
    } else if (selectedItemIndex > lastFullyVisibleItemIndex) {
        lazyListState.animateScrollToItem(
            selectedItemIndex - maximumFullyVisibleItemCount,
            cutOffItemHeight
        )
    }
}
