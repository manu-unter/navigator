import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test

class SequentiallyDoubleClickableTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `call onClick immediately when a click comes in`() {
        var wasOnClickCalled = false

        with(composeTestRule) {
            setContent {
                Box(
                    Modifier.sequentiallyDoubleClickable(
                        onClick = { wasOnClickCalled = true },
                        onDoubleClick = { fail("onDoubleClick should not be called on the first click") }
                    )
                )
            }

            onRoot().performClick()

            assert(wasOnClickCalled)
        }
    }
}