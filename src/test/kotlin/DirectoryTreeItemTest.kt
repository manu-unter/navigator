import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import model.Expandable
import model.Node
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

class DirectoryTreeItemTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `appearance of a ViewNode without children`() {
        val testNode = object : Node {
            override val label = "Test Node"
        }
        val viewNode = ViewNode(testNode)

        with(composeTestRule) {
            setContent { DirectoryTreeItem(viewNode, isSelected = false, onSelect = {}, isFocused = false) }

            onNodeWithContentDescription("Collapse icon").assertDoesNotExist()
            onNodeWithContentDescription("Expand icon").assertDoesNotExist()
        }
    }

    @Test
    fun `appearance of a theoretically expanded ViewNode with practically no children`() {
        val testNode = object : Node, Expandable {
            override val label = "Test Node"
            override fun listChildren(): List<Node> {
                return emptyList()
            }
        }
        val viewNode = ViewNode(testNode)

        with(composeTestRule) {
            setContent { DirectoryTreeItem(viewNode, isSelected = false, onSelect = {}, isFocused = false) }

            onNodeWithContentDescription("Collapse icon").assertDoesNotExist()
            onNodeWithContentDescription("Expand icon").assertDoesNotExist()
        }
    }

    @Test
    fun `appearance of an expanded ViewNode with children`() {
        val testNode = object : Node, Expandable {
            override val label = "Test Node"
            override fun listChildren(): List<Node> {
                val childNode = object : Node {
                    override val label = "Test Child Node"
                }
                return listOf(childNode)
            }
        }
        val viewNode = ViewNode(testNode)

        with(composeTestRule) {
            setContent { DirectoryTreeItem(viewNode, isSelected = false, onSelect = {}, isFocused = false) }

            onNodeWithContentDescription("Collapse icon").assertExists()
        }
    }

    @Test
    fun `appearance of a collapsed ViewNode with children`() {
        val testNode = object : Node, Expandable {
            override val label = "Test Node"
            override fun listChildren(): List<Node> {
                val childNode = object : Node {
                    override val label = "Test Child Node"
                }
                return listOf(childNode)
            }
        }
        val viewNode = ViewNode(testNode)
        viewNode.isExpanded = false

        with(composeTestRule) {
            setContent { DirectoryTreeItem(viewNode, isSelected = false, onSelect = {}, isFocused = false) }

            onNodeWithContentDescription("Expand icon").assertExists()
        }
    }

    @Test
    fun `appearance of an unselected ViewNode`() {
        val testNode = object : Node {
            override val label = "Test Node"
        }
        val viewNode = ViewNode(testNode)

        with(composeTestRule) {
            setContent { DirectoryTreeItem(viewNode, isSelected = false, onSelect = {}, isFocused = false) }

            onNodeWithText("Test Node").assertIsSelectable()
            onNodeWithText("Test Node").assertIsNotSelected()
        }
    }

    @Test
    fun `appearance of a selected ViewNode`() {
        val testNode = object : Node {
            override val label = "Test Node"
        }
        val viewNode = ViewNode(testNode)

        with(composeTestRule) {
            setContent { DirectoryTreeItem(viewNode, isSelected = true, onSelect = {}, isFocused = false) }

            onNodeWithText("Test Node").assertIsSelectable()
            onNodeWithText("Test Node").assertIsSelected()
        }
    }

    @Test
    fun `automatic listing of children for a ViewNode with children`() {
        val testNode = object : Node, Expandable {
            override val label = "Test Node"
            override fun listChildren(): List<Node> {
                val childNode = object : Node {
                    override val label = "Test Child Node"
                }
                return listOf(childNode)
            }
        }
        val viewNode = ViewNode(testNode)

        with(composeTestRule) {
            setContent { DirectoryTreeItem(viewNode, isSelected = false, onSelect = {}, isFocused = false) }

            waitForIdle()

            assertNotNull(viewNode.firstChild)
        }
    }

    @Test
    fun `expanding a ViewNode with children by clicking the icon`() {
        val testNode = object : Node, Expandable {
            override val label = "Test Node"
            override fun listChildren(): List<Node> {
                val childNode = object : Node {
                    override val label = "Test Child Node"
                }
                return listOf(childNode)
            }
        }
        val viewNode = ViewNode(testNode)
        viewNode.isExpanded = false

        with(composeTestRule) {
            setContent { DirectoryTreeItem(viewNode, isSelected = false, onSelect = {}, isFocused = false) }

            onNodeWithContentDescription("Expand icon").performClick()

            waitForIdle()

            assertEquals(true, viewNode.isExpanded)
            onNodeWithContentDescription("Collapse icon").assertExists()
        }
    }

    @Test
    fun `collapsing a ViewNode with children by clicking the icon`() {
        val testNode = object : Node, Expandable {
            override val label = "Test Node"
            override fun listChildren(): List<Node> {
                val childNode = object : Node {
                    override val label = "Test Child Node"
                }
                return listOf(childNode)
            }
        }
        val viewNode = ViewNode(testNode)
        viewNode.isExpanded = true

        with(composeTestRule) {
            setContent { DirectoryTreeItem(viewNode, isSelected = false, onSelect = {}, isFocused = false) }

            onNodeWithContentDescription("Collapse icon").performClick()

            waitForIdle()

            assertEquals(false, viewNode.isExpanded)
            onNodeWithContentDescription("Expand icon").assertExists()
        }
    }

    @Ignore("Activate once mainClock is implemented")
    @Test
    fun `expanding a ViewNode with children by double-clicking`() {
        val testNode = object : Node, Expandable {
            override val label = "Test Node"
            override fun listChildren(): List<Node> {
                val childNode = object : Node {
                    override val label = "Test Child Node"
                }
                return listOf(childNode)
            }
        }
        val viewNode = ViewNode(testNode)
        viewNode.isExpanded = false

        with(composeTestRule) {
            setContent { DirectoryTreeItem(viewNode, isSelected = false, onSelect = {}, isFocused = false) }

            onRoot().performClick()
            mainClock.advanceTimeBy(100)
            onRoot().performClick()

            waitForIdle()

            assertEquals(true, viewNode.isExpanded)
            onNodeWithContentDescription("Collapse icon").assertExists()
        }
    }

    @Ignore("Activate once mainClock is implemented")
    @Test
    fun `collapsing a ViewNode with children by double-clicking`() {
        val testNode = object : Node, Expandable {
            override val label = "Test Node"
            override fun listChildren(): List<Node> {
                val childNode = object : Node {
                    override val label = "Test Child Node"
                }
                return listOf(childNode)
            }
        }
        val viewNode = ViewNode(testNode)
        viewNode.isExpanded = true

        with(composeTestRule) {
            setContent { DirectoryTreeItem(viewNode, isSelected = false, onSelect = {}, isFocused = false) }

            onRoot().performClick()
            mainClock.advanceTimeBy(100)
            onRoot().performClick()

            waitForIdle()

            assertEquals(false, viewNode.isExpanded)
            onNodeWithContentDescription("Expand icon").assertExists()
        }
    }
}