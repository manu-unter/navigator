import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

class SequentiallyDoubleClickableTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Ignore("This doesn't work and I don't know why")
    @Test
    fun `calling onClick immediately when a click comes in`() {
        var wasOnClickCalled = false

        with(composeTestRule) {
            setContent {
                Box(
                    Modifier.sequentiallyDoubleClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { wasOnClickCalled = true },
                        onDoubleClick = {}
                    )
                )
            }

            onNode(hasClickAction()).performClick()
            waitForIdle()

            assertTrue(wasOnClickCalled)
        }
    }

    @Ignore("Activate once mainClock has been implemented")
    @Test
    fun `calling onDoubleClick when the second click comes in`() {
        var wasOnDoubleClickCalled = false

        with(composeTestRule) {
            setContent {
                Box(
                    Modifier.sequentiallyDoubleClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { },
                        onDoubleClick = { wasOnDoubleClickCalled = true }
                    )
                )
            }

            onNode(hasClickAction()).performClick()
            mainClock.advanceTimeBy(100)
            onNode(hasClickAction()).performClick()
            waitForIdle()

            assertTrue(wasOnDoubleClickCalled)
        }
    }

    @Test
    fun `not calling onDoubleClick when the second click comes in too early`() {
        var wasOnDoubleClickCalled = false

        with(composeTestRule) {
            setContent {
                Box(
                    Modifier.sequentiallyDoubleClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { },
                        onDoubleClick = { wasOnDoubleClickCalled = true }
                    )
                )
            }

            onNode(hasClickAction()).performClick()
            onNode(hasClickAction()).performClick()
            waitForIdle()

            assertFalse(wasOnDoubleClickCalled)
        }
    }

    @Ignore("Activate once mainClock has been implemented")
    @Test
    fun `not calling onDoubleClick when the second click comes in too late`() {
        var wasOnDoubleClickCalled = false

        with(composeTestRule) {
            setContent {
                Box(
                    Modifier.sequentiallyDoubleClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { },
                        onDoubleClick = { wasOnDoubleClickCalled = true }
                    )
                )
            }

            onNode(hasClickAction()).performClick()
            mainClock.advanceTimeBy(500)
            onNode(hasClickAction()).performClick()
            waitForIdle()

            assertFalse(wasOnDoubleClickCalled)
        }
    }
}