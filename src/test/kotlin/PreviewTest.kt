import androidx.compose.ui.test.IdlingResource
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import model.ContentReadable
import model.Node
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class PreviewTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `show a text preview for a node with contentType text plain`() {
        val testContent = "Test Text Content"
        val testNode = object : Node, ContentReadable {
            override val label = "Test Text Node"
            override val contentType = "text/plain"
            override fun contentInputStream(): InputStream {
                return testContent.byteInputStream()
            }
        }

        with(composeTestRule) {
            setContent { Preview(testNode) }

            // TODO Activate once this is implemented
//            onNodeWithText(testContent).assertIsDisplayed()
            onNodeWithText(testContent).assertExists()
        }
    }

    @Test
    fun `show an image preview for a node with contentType image png`() {
        val testFile = File("src/test/resources/test-image.png")
        val testNode = object : Node, ContentReadable, IdlingResource {
            override val label = "Test PNG Node"
            override val contentType = "image/png"
            override var isIdleNow = false

            override fun contentInputStream(): InputStream {
                return object : FileInputStream(testFile) {
                    init {
                        isIdleNow = false
                    }

                    override fun close() {
                        super.close()
                        isIdleNow = true
                    }
                }
            }
        }

        with(composeTestRule) {
            registerIdlingResource(testNode)

            setContent { Preview(testNode) }

            // TODO Activate once this is implemented
//            onNodeWithContentDescription("Image preview").assertIsDisplayed()
            onNodeWithContentDescription("Image preview").assertExists()
        }
    }

    @Test
    fun `show an image preview for a node with contentType image svg+xml`() {
        val testFile = File("src/test/resources/test-image.svg")
        val testNode = object : Node, ContentReadable, IdlingResource {
            override val label = "Test SVG Node"
            override val contentType = "image/svg+xml"
            override var isIdleNow = false

            override fun contentInputStream(): InputStream {
                return object : FileInputStream(testFile) {
                    init {
                        isIdleNow = false
                    }

                    override fun close() {
                        super.close()
                        isIdleNow = true
                    }
                }
            }
        }

        with(composeTestRule) {
            registerIdlingResource(testNode)

            setContent { Preview(testNode) }

            // TODO Activate once this is implemented
//            onNodeWithContentDescription("Image preview").assertIsDisplayed()
            onNodeWithContentDescription("Image preview").assertExists()
        }
    }

    @Test
    fun `show a message saying no preview is available for a node which is not ContentReadable`() {
        val testNode = object : Node {
            override val label = "Test Node Without Content"
        }

        with(composeTestRule) {
            setContent { Preview(testNode) }

            // TODO Activate once this is implemented
//            onNodeWithText("No preview available for this file type").assertIsDisplayed()
            onNodeWithText("No preview available for this file type").assertExists()
        }
    }

    @Test
    fun `show a message saying no preview is available for a node with contentType video`() {
        val testNode = object : Node, ContentReadable {
            override val label = "Test Video Node"
            override val contentType = "video/mp4"
            override fun contentInputStream(): InputStream {
                throw NotImplementedError()
            }
        }

        with(composeTestRule) {
            setContent { Preview(testNode) }

            // TODO Activate once this is implemented
//            onNodeWithText("No preview available for this file type").assertIsDisplayed()
            onNodeWithText("No preview available for this file type").assertExists()
        }
    }

    @Test
    fun `show a message asking to select a file when node is null`() {
        with(composeTestRule) {
            setContent { Preview(null) }

            // TODO Activate once this is implemented
//            onNodeWithText("Select a file to see a preview").assertIsDisplayed()
            onNodeWithText("Select a file to see a preview").assertExists()
        }
    }

    @Test
    fun `show an error message when the content of a text node cannot be read`() {
        val testNode = object : Node, ContentReadable {
            override val label = "Test Text Node"
            override val contentType = "text/plain"
            override fun contentInputStream(): InputStream {
                throw Error()
            }
        }

        with(composeTestRule) {
            setContent { Preview(testNode) }

            // TODO Activate once this is implemented
//            onNodeWithText("Select a file to see a preview").assertIsDisplayed()
            onNodeWithText("Could not read file for preview").assertExists()
        }
    }

    @Test
    fun `show an error message when the content of an image node cannot be read`() {
        val testNode = object : Node, ContentReadable {
            override val label = "Test Image Node"
            override val contentType = "image/png"
            override fun contentInputStream(): InputStream {
                throw Error()
            }
        }

        with(composeTestRule) {
            setContent { Preview(testNode) }

            // TODO Activate once this is implemented
//            onNodeWithText("Select a file to see a preview").assertIsDisplayed()
            onNodeWithText("Could not read file for preview").assertExists()
        }
    }
}