import androidx.compose.desktop.Window
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import java.io.File

fun main() = Window {
    var path by remember { mutableStateOf<String>("")}
    val fileSystemWalkerScope = rememberCoroutineScope { Dispatchers.IO }
    var currentFileWalkerJob by remember { mutableStateOf<Job?>(null)}
    var fileTreeWalk by remember { mutableStateOf<FileTreeWalk?>(null)}

    fun readFileTree(path: String) {
        currentFileWalkerJob?.cancel()
        currentFileWalkerJob = fileSystemWalkerScope.async {
            println(coroutineContext)
            fileTreeWalk = File(path).walk()
        }
    }

    MaterialTheme {
        Row {
            TextField(path, onValueChange = {
                path = it
                readFileTree(it)
            })
        }
        Row {
            if (fileTreeWalk != null) {
                LazyColumn {
                    fileTreeWalk?.forEach {
                        item {
                            Row {
                                Text(it.name)
                            }
                        }
                    }
                }
            }
        }
    }
}