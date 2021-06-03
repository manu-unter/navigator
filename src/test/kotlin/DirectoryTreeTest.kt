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
import org.junit.Before
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
    private val viewNode = ViewNode(testNode)
    private var listOfViewNodes: List<ViewNode> = emptyList()

    @Before
    fun setup() {
        viewNode.isExpanded = true
        viewNode.initChildren()
        val mutableListOfViewNodes = mutableListOf<ViewNode>()
        viewNode.addVisibleViewNodesDepthFirst(mutableListOfViewNodes)
        listOfViewNodes = mutableListOfViewNodes
    }

    @Test
    fun `creating a DirectoryTree from a list of ViewNodes`() {
        with(composeTestRule) {
            setContent { DirectoryTree(listOfViewNodes, selectedViewNode = null, onSelect = {}) }

            onNodeWithText(testNodeLabel).assertExists()
            onNodeWithText(testChildNodeLabel).assertExists()
            onNodeWithText(testGrandChildNodeLabel).assertDoesNotExist()
        }
    }

    @Test
    fun focusability() {
        with(composeTestRule) {
            setContent { DirectoryTree(listOfViewNodes, selectedViewNode = null, onSelect = {}) }

            onNode(isFocusable()).assertExists()

            onRoot().performClick()
            waitForIdle()

            onNode(isFocusable()).assertIsFocused()
        }
    }

    @Test
    fun `initial selection state`() {
        with(composeTestRule) {
            setContent { DirectoryTree(listOfViewNodes, selectedViewNode = listOfViewNodes[0], onSelect = {}) }

            onNodeWithText(testNodeLabel).assertIsSelectable()
            onNodeWithText(testNodeLabel).assertIsSelected()
            onNodeWithText(testChildNodeLabel).assertIsSelectable()
            onNodeWithText(testChildNodeLabel).assertIsNotSelected()
        }
    }

    @Test
    fun `selecting an item by clicking it`() {
        with(composeTestRule) {
            var selectedViewNode: ViewNode? by mutableStateOf(listOfViewNodes[0])
            setContent { DirectoryTree(listOfViewNodes, selectedViewNode, onSelect = { selectedViewNode = it }) }

            onNodeWithText(testChildNodeLabel).performClick()
            waitForIdle()

            onNodeWithText(testChildNodeLabel).assertIsSelectable()
            onNodeWithText(testChildNodeLabel).assertIsSelected()
        }
    }

    @Test
    fun `clearing the selection by pressing Escape`() {
        with(composeTestRule) {
            var selectedViewNode: ViewNode? by mutableStateOf(listOfViewNodes[0])
            setContent { DirectoryTree(listOfViewNodes, selectedViewNode, onSelect = { selectedViewNode = it }) }

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
            var selectedViewNode: ViewNode? by mutableStateOf(listOfViewNodes[0])
            setContent { DirectoryTree(listOfViewNodes, selectedViewNode, onSelect = { selectedViewNode = it }) }

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
            var selectedViewNode: ViewNode? by mutableStateOf(listOfViewNodes[1])
            setContent { DirectoryTree(listOfViewNodes, selectedViewNode, onSelect = { selectedViewNode = it }) }

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
        viewNode.isExpanded = false

        with(composeTestRule) {
            var selectedViewNode: ViewNode? by mutableStateOf(listOfViewNodes[0])
            setContent { DirectoryTree(listOfViewNodes, selectedViewNode, onSelect = { selectedViewNode = it }) }

            onNode(isFocusable()).performClick()
            waitForIdle()
            onRoot().performKeyPress(keyEvent(Key.DirectionRight, KeyEventType.KeyDown))
            waitForIdle()

            assertEquals(true, viewNode.isExpanded)
            onNodeWithText(testNodeLabel).assertIsNotSelected()
            onNodeWithText(testChildNodeLabel).assertIsSelected()
        }
    }

    @Test
    fun `collapsing the parent node and selecting it first child by pressing the left arrow`() {
        viewNode.isExpanded = true
        viewNode.firstChild!!.isExpanded = true
        viewNode.firstChild!!.initChildren()
        val mutableListOfViewNodes = mutableListOf<ViewNode>()
        viewNode.addVisibleViewNodesDepthFirst(mutableListOfViewNodes)
        listOfViewNodes = mutableListOfViewNodes

        with(composeTestRule) {
            var selectedViewNode: ViewNode? by mutableStateOf(listOfViewNodes[2])
            setContent { DirectoryTree(listOfViewNodes, selectedViewNode, onSelect = { selectedViewNode = it }) }

            onNode(isFocusable()).performClick()
            waitForIdle()
            onRoot().performKeyPress(keyEvent(Key.DirectionLeft, KeyEventType.KeyDown))
            waitForIdle()

            assertEquals(false, viewNode.firstChild!!.isExpanded)
            onNodeWithText(testNodeLabel).assertIsNotSelected()
            onNodeWithText(testChildNodeLabel).assertIsSelected()
            onNodeWithText(testGrandChildNodeLabel).assertIsNotSelected()
        }
    }
}