import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import java.io.File

class NavigatorTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `creating the Navigator`() {
        with(composeTestRule) {
            setContent { Navigator() }

            val initialRootPath = System.getProperty("user.home")
            val initialRootNodeLabel = File(initialRootPath).canonicalFile.name

            onNodeWithText(initialRootPath).assertExists()
            onNodeWithText(initialRootNodeLabel).assertExists()
        }
    }

    @Ignore("Activate once performTextReplacement is implemented")
    @Test
    fun `changing the root path to an existing directory`() {
        with(composeTestRule) {
            setContent { Navigator() }

            val initialRootPath = System.getProperty("user.home")
            onNodeWithText(initialRootPath).performTextReplacement("./src/test/resources")

            onNodeWithText("resources").assertExists()
            onNodeWithText("test-image.png").assertExists()
            onNodeWithText("test-image.svg").assertExists()
            onNodeWithText("test-text-file.txt").assertExists()
        }
    }

    @Ignore("Activate once performTextReplacement is implemented")
    @Test
    fun `changing the root path to an invalid directory`() {
        with(composeTestRule) {
            setContent { Navigator() }

            val initialRootPath = System.getProperty("user.home")
            onNodeWithText(initialRootPath).performTextReplacement("/something-which-hopefully-is-not-there")

            onNodeWithText("Please provide a valid path").assertExists()
        }
    }

    @Ignore("Activate once performTextReplacement is implemented")
    @Test
    fun `selecting a text file`() {
        with(composeTestRule) {
            setContent { Navigator() }

            val initialRootPath = System.getProperty("user.home")
            onNodeWithText(initialRootPath).performTextReplacement("./src/test/resources")
            onNodeWithText("test-text-file.txt").performClick()

            onNodeWithContentDescription("Text preview").assertExists()
            onNodeWithText("Test Text Content").assertExists()
        }
    }
}