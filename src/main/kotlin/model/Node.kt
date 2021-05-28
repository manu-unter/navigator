import java.io.File

interface Node {
    val label: String
    fun listChildren(): List<Node>
}

class FileSystemNode(private val file: File) : Node {
    override val label: String get() = file.name
    override fun listChildren(): List<Node> {
        return if (file.isDirectory) {
            // Some directories can't be listed and will return null
            file.listFiles()?.let { arrayOfFiles ->
                arrayOfFiles.sortWith(comparator = compareBy({ !it.isDirectory }, { it.name }))
                arrayOfFiles.map { FileSystemNode(it) }
            }
                ?: emptyList()
        } else {
            emptyList()
        }
    }
}