import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import model.FileSystemNode
import model.Node
import org.jetbrains.skija.Image
import java.io.File
import java.net.URLConnection

@Composable
fun Preview(node: Node?, modifier: Modifier = Modifier) {
    Box(modifier.padding(12.dp), Alignment.Center) {
        if (node != null && node is FileSystemNode && node.file.isFile) {
            val contentType = URLConnection.guessContentTypeFromName(node.file.name)
            val mediaType = contentType?.split("/")?.get(0)
            when (mediaType) {
                "text" -> TextPreview(node.file)
                "image" -> ImagePreview(node.file)
                else -> Text("No preview available for this file type")
            }
        } else {
            Text("Select a file to see a preview")
        }
    }

}

@Composable
fun TextPreview(file: File) {
    val contentPreview by produceState<String?>(initialValue = null, file) {
        withContext(Dispatchers.IO) {
            value = file.readText()
        }
    }

    contentPreview?.let {
        Text(it, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun ImagePreview(file: File) {
    val imagePreview by produceState<ImageBitmap?>(initialValue = null, file) {
        withContext(Dispatchers.IO) {
            value = Image.makeFromEncoded(file.readBytes()).asImageBitmap()
        }
    }
    imagePreview?.let {
        Image(bitmap = it, contentDescription = "Image preview")
    }
}