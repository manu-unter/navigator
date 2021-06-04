import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.keyEvent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import model.Expandable
import model.Node
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DirectoryTreeTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val testGrandChildNodeLabel = "Test Grandchild Node"
    private val testGrandChildNode: Node = object : Node {
        override val label = testGrandChildNodeLabel
    }
    private val testChildNodeLabel = "Test Child Node"
    private val testChildNode: Node = object : Node, Expandable {
        override val label = testChildNodeLabel
        override fun listChildren(): List<Node> {
            return listOf(testGrandChildNode)
        }
    }
    private val testNodeLabel = "Test Expandable Node"
    private val testNode: Node = object : Node, Expandable {
        override val label = testNodeLabel
        override fun listChildren(): List<Node> {
            return listOf(testChildNode)
        }
    }
    private val rootViewNode = ViewNode(testNode)


    init {
        rootViewNode.initChildren()
    }

    @Test
    fun `creating a DirectoryTree from a list of ViewNodes`() {
        val selectionState: MutableState<ViewNode?> = mutableStateOf(null)

        with(composeTestRule) {
            setContent { DirectoryTree(rootViewNode, selectionState) }

            onNodeWithText(testNodeLabel).assertExists()
            onNodeWithText(testChildNodeLabel).assertExists()
            onNodeWithText(testGrandChildNodeLabel).assertDoesNotExist()
        }
    }

    @Test
    fun focusability() {
        val selectionState: MutableState<ViewNode?> = mutableStateOf(null)

        with(composeTestRule) {
            setContent { DirectoryTree(rootViewNode, selectionState) }

            onNode(isFocusable()).assertExists()

            onRoot().performClick()
            waitForIdle()

            onNode(isFocusable()).assertIsFocused()
        }
    }

    @Test
    fun `initial selection state`() {
        val selectionState: MutableState<ViewNode?> = mutableStateOf(rootViewNode)

        with(composeTestRule) {
            setContent { DirectoryTree(rootViewNode, selectionState) }

            onNodeWithText(testNodeLabel).assertIsSelectable()
            onNodeWithText(testNodeLabel).assertIsSelected()
            onNodeWithText(testChildNodeLabel).assertIsSelectable()
            onNodeWithText(testChildNodeLabel).assertIsNotSelected()
        }
    }

    @Test
    fun `selecting an item by clicking it`() {
        val selectionState: MutableState<ViewNode?> = mutableStateOf(rootViewNode)

        with(composeTestRule) {
            setContent { DirectoryTree(rootViewNode, selectionState) }

            onNodeWithText(testChildNodeLabel).performClick()
            waitForIdle()

            onNodeWithText(testChildNodeLabel).assertIsSelectable()
            onNodeWithText(testChildNodeLabel).assertIsSelected()
        }
    }

    @Test
    fun `clearing the selection by pressing Escape`() {
        val selectionState: MutableState<ViewNode?> = mutableStateOf(rootViewNode)

        with(composeTestRule) {
            setContent { DirectoryTree(rootViewNode, selectionState) }

            onNode(isFocusable()).performClick()
            waitForIdle()
            onRoot().performKeyPress(keyEvent(Key.Escape, KeyEventType.KeyDown))
            waitForIdle()

            onNodeWithText(testNodeLabel).assertIsNotSelected()
            onNodeWithText(testChildNodeLabel).assertIsNotSelected()
        }
    }

    @Test
    fun `selecting the next item by pressing the down arrow`() {
        val selectionState: MutableState<ViewNode?> = mutableStateOf(rootViewNode)

        with(composeTestRule) {
            setContent { DirectoryTree(rootViewNode, selectionState) }

            onNode(isFocusable()).performClick()
            waitForIdle()
            onRoot().performKeyPress(keyEvent(Key.DirectionDown, KeyEventType.KeyDown))
            waitForIdle()

            onNodeWithText(testNodeLabel).assertIsNotSelected()
            onNodeWithText(testChildNodeLabel).assertIsSelected()
        }
    }

    @Test
    fun `selecting the previous item by pressing the up arrow`() {
        val selectionState: MutableState<ViewNode?> = mutableStateOf(rootViewNode.firstChild!!)

        with(composeTestRule) {
            setContent { DirectoryTree(rootViewNode, selectionState) }

            onNode(isFocusable()).performClick()
            waitForIdle()
            onRoot().performKeyPress(keyEvent(Key.DirectionUp, KeyEventType.KeyDown))
            waitForIdle()

            onNodeWithText(testNodeLabel).assertIsSelected()
            onNodeWithText(testChildNodeLabel).assertIsNotSelected()
        }
    }

    @Test
    fun `expanding a node and selecting its first child by pressing the right arrow`() {
        rootViewNode.isExpanded = false
        val selectionState: MutableState<ViewNode?> = mutableStateOf(rootViewNode)

        with(composeTestRule) {
            setContent { DirectoryTree(rootViewNode, selectionState) }

            onNode(isFocusable()).performClick()
            waitForIdle()
            onRoot().performKeyPress(keyEvent(Key.DirectionRight, KeyEventType.KeyDown))
            waitForIdle()

            assertEquals(true, rootViewNode.isExpanded)
            onNodeWithText(testNodeLabel).assertIsNotSelected()
            onNodeWithText(testChildNodeLabel).assertIsSelected()
        }
    }

    @Test
    fun `collapsing the parent node and selecting its first child by pressing the left arrow`() {
        rootViewNode.isExpanded = true
        rootViewNode.firstChild!!.isExpanded = true
        rootViewNode.firstChild!!.initChildren()
        val selectionState: MutableState<ViewNode?> = mutableStateOf(rootViewNode.firstChild!!.firstChild!!)

        with(composeTestRule) {
            setContent { DirectoryTree(rootViewNode, selectionState) }

            onNode(isFocusable()).performClick()
            waitForIdle()
            onRoot().performKeyPress(keyEvent(Key.DirectionLeft, KeyEventType.KeyDown))
            waitForIdle()

            assertEquals(false, rootViewNode.firstChild!!.isExpanded)
            onNodeWithText(testNodeLabel).assertIsNotSelected()
            onNodeWithText(testChildNodeLabel).assertIsSelected()
            onNodeWithText(testGrandChildNodeLabel).assertDoesNotExist()
        }
    }
}