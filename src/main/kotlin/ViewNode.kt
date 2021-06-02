import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import model.Expandable
import model.Node

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

    /**
     * Adds this ViewNode to the given list and, if it is expanded, recursively calls this same method on its children.
     * Entries are listed in depth-first order to conform with the required ordering for listing in a flat column.
     */
    fun addVisibleViewNodesDepthFirst(listOfExpandedViewNodes: MutableList<ViewNode>) {
        listOfExpandedViewNodes += this

        if (isExpanded) {
            children?.forEach {
                it.addVisibleViewNodesDepthFirst(listOfExpandedViewNodes)
            }
        }
    }
}
