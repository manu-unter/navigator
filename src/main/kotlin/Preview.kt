import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
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
import model.ContentReadable
import model.Node
import org.jetbrains.skija.Image
import java.io.InputStream

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

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun TextPreview(contentReadable: ContentReadable) {
    val previewTextResult by produceState<Result<String>?>(initialValue = null, contentReadable) {
        withContext(Dispatchers.IO) {
            var contentInputStream: InputStream? = null
            value = try {
                contentInputStream = contentReadable.contentInputStream()
                Result.success(contentInputStream.reader().readText())
            } catch (throwable: Throwable) {
                Result.failure(throwable)
            } finally {
                @Suppress("BlockingMethodInNonBlockingContext")
                contentInputStream?.close()
            }
        }
    }

    AnimatedVisibility(
        visible = previewTextResult != null,
        enter = fadeIn() + slideInHorizontally(initialOffsetX = { -it / 8 })
    ) {
        if (previewTextResult!!.isSuccess) {
            Text(previewTextResult!!.getOrThrow(), fontFamily = FontFamily.Monospace)
        } else {
            ErrorMessage("Could not read file for preview")
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ImagePreview(contentReadable: ContentReadable) {
    val localDensity = LocalDensity.current
    val imagePreviewPainter by produceState<Result<Painter>?>(initialValue = null, contentReadable, localDensity) {
        withContext(Dispatchers.IO) {
            var contentInputStream: InputStream? = null
            value = try {
                contentInputStream = contentReadable.contentInputStream()

                @Suppress("BlockingMethodInNonBlockingContext")
                val painter = when (contentReadable.contentType) {
                    "image/svg+xml" -> loadSvgResource(contentInputStream, localDensity)
                    else -> BitmapPainter(
                        Image.makeFromEncoded(contentInputStream.readAllBytes()).asImageBitmap()
                    )
                }

                Result.success(painter)
            } catch (throwable: Throwable) {
                Result.failure(throwable)
            } finally {
                @Suppress("BlockingMethodInNonBlockingContext")
                contentInputStream?.close()
            }
        }
    }

    AnimatedVisibility(
        visible = imagePreviewPainter != null,
        enter = fadeIn() + slideInHorizontally(initialOffsetX = { -it / 8 }),
    ) {
        if (imagePreviewPainter!!.isSuccess) {
            Image(painter = imagePreviewPainter!!.getOrThrow(), contentDescription = "Image preview")
        } else {
            ErrorMessage("Could not read file for preview")
        }
    }
}

@Composable
private fun DimmedMessage(message: String) {
    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
        Text(message)
    }
}

@Composable
private fun ErrorMessage(message: String) {
    Surface(color = MaterialTheme.colors.error) {
        Text(message, Modifier.padding(12.dp))
    }
}