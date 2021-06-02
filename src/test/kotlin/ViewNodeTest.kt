import model.Expandable
import model.Node
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test


class ViewNodeTest {
    @Test
    fun `the root node is expanded by default if the Node is Expandable`() {
        val testNode: Node = object : Node, Expandable {
            override val label = "Test Expandable Node"
            override fun listChildren(): List<Node> {
                fail("listChildren should not be called eagerly by ViewNode itself")
                throw Error()
            }
        }

        val viewNode = ViewNode(testNode)

        assertEquals(true, viewNode.isExpanded)
    }

    @Test
    fun `the children are only initialized when initChildren is called`() {
        val testChildNode = object : Node {
            override val label = "Test Child Node"
        }
        val testNode: Node = object : Node, Expandable {
            override val label = "Test Expandable Node"
            override fun listChildren(): List<Node> {
                return listOf(testChildNode)
            }
        }

        val viewNode = ViewNode(testNode)

        assertEquals(false, viewNode.hasChildren())
        assertEquals(null, viewNode.firstChild)

        viewNode.initChildren()

        assertEquals(true, viewNode.hasChildren())
        assertEquals(testChildNode, viewNode.firstChild?.node)
    }

    @Test
    fun `level tracking`() {
        val testGrandChildNode = object : Node {
            override val label = "Test Grandchild Node"
        }
        val testChildNode: Node = object : Node, Expandable {
            override val label = "Test Expandable Child Node"
            override fun listChildren(): List<Node> {
                return listOf(testGrandChildNode)
            }
        }
        val testNode: Node = object : Node, Expandable {
            override val label = "Test Expandable Node"
            override fun listChildren(): List<Node> {
                return listOf(testChildNode)
            }
        }

        val viewNode = ViewNode(testNode)
        viewNode.initChildren()
        viewNode.firstChild!!.initChildren()
        viewNode.firstChild!!.firstChild!!.initChildren()

        assertEquals(0, viewNode.level)
        assertEquals(1, viewNode.firstChild!!.level)
        assertEquals(2, viewNode.firstChild!!.firstChild!!.level)
    }

    @Test
    fun `listing visible ViewNodes with addExpandedViewNodesDepthFirst`() {
        val testGrandChildNode = object : Node {
            override val label = "Test Grandchild Node"
        }
        val testChildNodeA: Node = object : Node, Expandable {
            override val label = "Test Expandable Child Node"
            override fun listChildren(): List<Node> {
                return listOf(testGrandChildNode)
            }
        }
        val testHiddenGrandChildNode = object : Node {
            override val label = "Test Hidden Grandchild Node"
        }
        val testChildNodeB = object : Node, Expandable {
            override val label = "Test Child Node"
            override fun listChildren(): List<Node> {
                return listOf(testHiddenGrandChildNode)
            }
        }
        val testNode: Node = object : Node, Expandable {
            override val label = "Test Expandable Node"
            override fun listChildren(): List<Node> {
                return listOf(testChildNodeA, testChildNodeB)
            }
        }

        val viewNode = ViewNode(testNode)
        viewNode.initChildren()
        viewNode.firstChild!!.initChildren()
        viewNode.firstChild!!.isExpanded = true

        val listOfViewNodes = mutableListOf<ViewNode>()
        viewNode.addVisibleViewNodesDepthFirst(listOfViewNodes)
        assertEquals(
            mutableListOf(testNode.label, testChildNodeA.label, testGrandChildNode.label, testChildNodeB.label),
            listOfViewNodes.map { it.node.label },
        )
    }
}