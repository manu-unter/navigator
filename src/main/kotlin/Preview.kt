import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadSvgResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.*
import org.jetbrains.skija.Image

@Composable
fun Preview(node: Node?, modifier: Modifier = Modifier) {
    Surface(color = MaterialTheme.colors.background, modifier = modifier) {
        Box(
            Modifier.padding(12.dp), Alignment.Center
        ) {
            if (node != null) {
                if (node is ContentReadable) {
                    @Suppress("BlockingMethodInNonBlockingContext")
                    when (node.contentType.split("/")[0]) {
                        "text" -> TextPreview(node)
                        "image" -> ImagePreview(node)
                        else -> Text("No preview available for this file type")
                    }
                } else {
                    Text("No preview available for this file type")
                }
            } else {
                Text("Select a file to see a preview")
            }
        }
    }
}

@Composable
private fun TextPreview(contentReadable: ContentReadable) {
    val previewText by produceState<String?>(initialValue = null, contentReadable) {
        withContext(Dispatchers.IO) {
            value = contentReadable.contentInputStream().reader().readText()
        }
    }

    previewText?.let {
        Text(it, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun ImagePreview(contentReadable: ContentReadable) {
    val localDensity = LocalDensity.current
    val imagePreviewPainter by produceState<Painter?>(initialValue = null, contentReadable, localDensity) {
        withContext(Dispatchers.IO) {
            val contentInputStream = contentReadable.contentInputStream()

            @Suppress("BlockingMethodInNonBlockingContext")
            value = when (contentReadable.contentType) {
                "image/svg+xml" -> loadSvgResource(contentInputStream, localDensity)
                else -> BitmapPainter(
                    Image.makeFromEncoded(contentInputStream.readAllBytes()).asImageBitmap()
                )
            }
        }
    }

    imagePreviewPainter?.let {
        Image(painter = it, contentDescription = "Image preview")
    }
}