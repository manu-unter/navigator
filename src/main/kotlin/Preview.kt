import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
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

@ExperimentalAnimationApi
@Composable
fun Preview(node: Node?, modifier: Modifier = Modifier) {
    Surface(color = MaterialTheme.colors.background, modifier = modifier.padding(12.dp)) {
        Crossfade(targetState = node, animationSpec = tween(150), modifier = Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                if (it != null) {
                    if (it is ContentReadable) {
                        @Suppress("BlockingMethodInNonBlockingContext")
                        when (it.contentType.split("/")[0]) {
                            "text" -> TextPreview(it)
                            "image" -> ImagePreview(it)
                            else -> DimmedMessage("No preview available for this file type")
                        }
                    } else {
                        DimmedMessage("No preview available for this file type")
                    }
                } else {
                    DimmedMessage("Select a file to see a preview")
                }
            }
        }
    }
}

@ExperimentalAnimationApi
@Composable
private fun TextPreview(contentReadable: ContentReadable) {
    val previewText by produceState<String?>(initialValue = null, contentReadable) {
        withContext(Dispatchers.IO) {
            value = contentReadable.contentInputStream().reader().readText()
        }
    }

    AnimatedVisibility(
        visible = previewText != null,
        enter = fadeIn() + slideInHorizontally(initialOffsetX = { -it / 8 })
    ) {
        Text(previewText!!, fontFamily = FontFamily.Monospace)
    }
}

@ExperimentalAnimationApi
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
    AnimatedVisibility(
        visible = imagePreviewPainter != null,
        enter = fadeIn() + slideInHorizontally(initialOffsetX = { -it / 8 }),
    ) {
        Image(painter = imagePreviewPainter!!, contentDescription = "Image preview")
    }
}

@Composable
private fun DimmedMessage(message: String) {
    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
        Text(message)
    }
}