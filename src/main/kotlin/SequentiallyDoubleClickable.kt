import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalViewConfiguration

/**
 * Makes the component both single- and double-clickable by *immediately* calling `onClick` on the first click and
 * *then* `onDoubleClick` when a second click comes in. Use this modifier to avoid the delay that the standard
 * `Modifier.combinedClickable` introduces when both an `onClick` and an `onDoubleClick` handler are given.
 *
 * We need to handle the double clicks in userland until the issue has been addressed in the Compose standard library.
 *
 * See https://github.com/JetBrains/compose-jb/issues/255 and https://issuetracker.google.com/issues/177929160
 */
@Composable
fun Modifier.sequentiallyDoubleClickable(
    interactionSource: MutableInteractionSource,
    indication: Indication?,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit
): Modifier {
    val doubleTapMinTimeMillis = LocalViewConfiguration.current.doubleTapMinTimeMillis
    val doubleTapTimeoutMillis = LocalViewConfiguration.current.doubleTapTimeoutMillis
    var lastClickTimeMillis: Long? by remember { mutableStateOf(null) }

    return clickable(interactionSource, indication) {
        val isDoubleClick =
            lastClickTimeMillis?.let {
                val millisSinceLastClick = System.currentTimeMillis() - it
                millisSinceLastClick in doubleTapMinTimeMillis until doubleTapTimeoutMillis
            } ?: false

        if (isDoubleClick) {
            lastClickTimeMillis = null
            onDoubleClick()
        } else {
            lastClickTimeMillis = System.currentTimeMillis()
            onClick()
        }
    }
}