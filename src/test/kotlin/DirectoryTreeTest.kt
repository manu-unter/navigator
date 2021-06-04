import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
        with(composeTestRule) {
            setContent { DirectoryTree(rootViewNode, selectedViewNode = null, onSelect = {}) }

            onNodeWithText(testNodeLabel).assertExists()
            onNodeWithText(testChildNodeLabel).assertExists()
            onNodeWithText(testGrandChildNodeLabel).assertDoesNotExist()
        }
    }

    @Test
    fun focusability() {
        with(composeTestRule) {
            setContent { DirectoryTree(rootViewNode, selectedViewNode = null, onSelect = {}) }

            onNode(isFocusable()).assertExists()

            onRoot().performClick()
            waitForIdle()

            onNode(isFocusable()).assertIsFocused()
        }
    }

    @Test
    fun `initial selection state`() {
        with(composeTestRule) {
            setContent { DirectoryTree(rootViewNode, selectedViewNode = rootViewNode, onSelect = {}) }

            onNodeWithText(testNodeLabel).assertIsSelectable()
            onNodeWithText(testNodeLabel).assertIsSelected()
            onNodeWithText(testChildNodeLabel).assertIsSelectable()
            onNodeWithText(testChildNodeLabel).assertIsNotSelected()
        }
    }

    @Test
    fun `selecting an item by clicking it`() {
        with(composeTestRule) {
            var selectedViewNode: ViewNode? by mutableStateOf(rootViewNode)
            setContent { DirectoryTree(rootViewNode, selectedViewNode, onSelect = { selectedViewNode = it }) }

            onNodeWithText(testChildNodeLabel).performClick()
            waitForIdle()

            onNodeWithText(testChildNodeLabel).assertIsSelectable()
            onNodeWithText(testChildNodeLabel).assertIsSelected()
        }
    }

    @Test
    fun `clearing the selection by pressing Escape`() {
        with(composeTestRule) {
            var selectedViewNode: ViewNode? by mutableStateOf(rootViewNode)
            setContent { DirectoryTree(rootViewNode, selectedViewNode, onSelect = { selectedViewNode = it }) }

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
        with(composeTestRule) {
            var selectedViewNode: ViewNode? by mutableStateOf(rootViewNode)
            setContent { DirectoryTree(rootViewNode, selectedViewNode, onSelect = { selectedViewNode = it }) }

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
        with(composeTestRule) {
            var selectedViewNode: ViewNode? by mutableStateOf(rootViewNode.firstChild!!)
            setContent { DirectoryTree(rootViewNode, selectedViewNode, onSelect = { selectedViewNode = it }) }

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

        with(composeTestRule) {
            var selectedViewNode: ViewNode? by mutableStateOf(rootViewNode)
            setContent { DirectoryTree(rootViewNode, selectedViewNode, onSelect = { selectedViewNode = it }) }

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

        with(composeTestRule) {
            var selectedViewNode: ViewNode? by mutableStateOf(rootViewNode.firstChild!!.firstChild!!)
            setContent { DirectoryTree(rootViewNode, selectedViewNode, onSelect = { selectedViewNode = it }) }

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