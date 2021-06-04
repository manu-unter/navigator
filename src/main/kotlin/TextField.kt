import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp

@Composable
fun TextField(value: String, onValueChange: (value: String) -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderWidth by animateDpAsState(
        if (isFocused) 2.dp else 1.dp,
        tween(durationMillis = 150)
    )

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current)),
        cursorBrush = SolidColor(MaterialTheme.colors.primary),
        interactionSource = interactionSource,
    ) { innerTextField ->
        Surface(
            color = MaterialTheme.colors.surface,
            elevation = 8.dp,
            border = if (isFocused) BorderStroke(width = borderWidth, color = MaterialTheme.colors.primary) else null,
        ) {
            Box(modifier = modifier.padding(8.dp, 4.dp)) {
                innerTextField()
            }
        }
    }
}