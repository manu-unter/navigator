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
import kotlinx.coroutines.launch
import java.lang.Integer.max
import java.lang.Integer.min

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
    val coroutineScope = rememberCoroutineScope()

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
                        val newSelectionIndex = currentSelectionIndex - 1
                        selectedViewNode = listOfViewNodes[newSelectionIndex]
                        coroutineScope.launch {
                            scrollItemIntoViewportIfNecessary(lazyListState, newSelectionIndex)
                        }
                    }
                }
                on(Key.DirectionDown) {
                    val currentSelectionIndex = listOfViewNodes.indexOf(selectedViewNode)
                    if (currentSelectionIndex < listOfViewNodes.lastIndex) {
                        val newSelectionIndex = currentSelectionIndex + 1
                        selectedViewNode = listOfViewNodes[newSelectionIndex]
                        coroutineScope.launch {
                            scrollItemIntoViewportIfNecessary(lazyListState, newSelectionIndex)
                        }
                    }
                }
                on(Key.DirectionLeft) {
                    selectedViewNode?.parent?.let {
                        selectedViewNode = it
                        it.isExpanded = false
                        coroutineScope.launch {
                            val newSelectionIndex = listOfViewNodes.indexOf(it)
                            scrollItemIntoViewportIfNecessary(lazyListState, newSelectionIndex)
                        }
                    }
                }
                on(Key.DirectionRight) {
                    selectedViewNode?.firstChild?.let {
                        selectedViewNode!!.isExpanded = true
                        selectedViewNode = it
                        coroutineScope.launch {
                            val newSelectionIndex = listOfViewNodes.indexOf(it)
                            scrollItemIntoViewportIfNecessary(lazyListState, newSelectionIndex)
                        }
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
 * Triggers animateScrollIntoView() with the appropriate parameters when necessary to move the given item fully into the
 * viewport.
 * This function assumes that all items have the same height!
 */
suspend fun scrollItemIntoViewportIfNecessary(lazyListState: LazyListState, itemIndex: Int, paddingItemCount: Int = 3) {
    if (lazyListState.layoutInfo.visibleItemsInfo.isEmpty() || itemIndex == -1) {
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
    val firstIndexToMakeVisible = max(0, itemIndex - paddingItemCount)
    val lastIndexToMakeVisible = min(lazyListState.layoutInfo.totalItemsCount - 1, itemIndex + paddingItemCount)

    if (firstIndexToMakeVisible < firstFullyVisibleItemIndex) {
        lazyListState.animateScrollToItem(firstIndexToMakeVisible)
    } else if (lastIndexToMakeVisible > lastFullyVisibleItemIndex) {
        lazyListState.animateScrollToItem(
            lastIndexToMakeVisible - maximumFullyVisibleItemCount,
            cutOffItemHeight
        )
    }
}
