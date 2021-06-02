import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogState

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AboutDialog(state: DialogState = DialogState()) =
    Dialog(title = "About", state = state, resizable = false) {
        val uriHandler = LocalUriHandler.current
        fun openAnnotatedUrl(annotatedString: AnnotatedString, offset: Int) {
            annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset).firstOrNull()
                ?.let { annotation -> uriHandler.openUri(annotation.item) }
        }

        Surface(color = MaterialTheme.colors.surface, modifier = Modifier.fillMaxSize()) {
            Column(Modifier.padding(12.dp)) {
                val contentTextStyle =
                    LocalTextStyle.current.copy(color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current))
                val linkSpanStyle = SpanStyle(
                    color = MaterialTheme.colors.primary,
                    textDecoration = TextDecoration.Underline
                )

                Text("Navigator", style = MaterialTheme.typography.h4)
                Spacer(Modifier.height(12.dp))
                val authorText = with(AnnotatedString.Builder()) {
                    append("Jetpack Compose Desktop application written by ")
                    pushStringAnnotation(tag = "URL", annotation = "https://manuscript.blog")
                    pushStyle(linkSpanStyle)
                    append("Manuel Unterhofer")
                    pop()
                    pop()
                    append(" as a test assignment for the JetBrains application process")
                    toAnnotatedString()
                }
                ClickableText(
                    text = authorText,
                    onClick = { offset -> openAnnotatedUrl(annotatedString = authorText, offset) },
                    style = contentTextStyle,
                )
                Divider(Modifier.padding(0.dp, 12.dp))
                val iconLicenseText = with(AnnotatedString.Builder()) {
                    append("Application icon created with a free license from ")
                    pushStringAnnotation(tag = "URL", annotation = "https://icons8.com")
                    pushStyle(linkSpanStyle)
                    append("ICONS8")
                    pop()
                    pop()
                    toAnnotatedString()
                }
                ClickableText(
                    text = iconLicenseText,
                    onClick = { offset -> openAnnotatedUrl(annotatedString = iconLicenseText, offset) },
                    style = contentTextStyle,
                )
            }
        }
    }