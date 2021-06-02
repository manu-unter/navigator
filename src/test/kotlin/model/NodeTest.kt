package model

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.io.File

class NodeTest {
    @Test
    fun `create a Node from a directory`() {
        val testFile = File("src/test/resources")
        val node = Node(testFile)
        val expectedChildren = testFile
            .listFiles()!!
            .sortedWith(comparator = compareBy({ !it.isDirectory }, { it.name }))
            .map { Node(it) }

        assertEquals(testFile.canonicalFile.name, node.label)
        if (node !is Expandable) {
            fail("A Node for a directory should be returned as Expandable")
        } else {
            assertEquals(expectedChildren.map { it.label }, node.listChildren().map { it.label })
        }
    }

    @Test
    fun `create a Node from a txt file`() {
        val testFile = File("src/test/resources/test-text-file.txt")
        val node = Node(testFile)
        val expectedContent = testFile.readText()

        assertEquals(node.label, testFile.canonicalFile.name)
        if (node !is ContentReadable) {
            fail("A Node for a txt file should be returned as ContentReadable")
        } else {
            assertEquals("text/plain", node.contentType)
            with(node.contentInputStream().reader()) {
                assertEquals(expectedContent, readText())
                close()
            }
        }
    }

    @Test
    fun `create a Node from a png file`() {
        val testFile = File("src/test/resources/test-image.png")
        val node = Node(testFile)

        assertEquals(testFile.canonicalFile.name, node.label)
        if (node !is ContentReadable) {
            fail("A Node for a png file should be returned as ContentReadable")
        } else {
            assertEquals("image/png", node.contentType)
        }
    }
}