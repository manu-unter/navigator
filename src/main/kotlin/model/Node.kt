package model

import java.io.File
import java.io.InputStream
import java.net.URLConnection


interface Node {
    val label: String
    fun listChildren(): List<Node>
}

fun Node(file: File): Node {
    // This method of inferring the content type is rather limited - it will only identify basic file extensions.
    // We could extend it with https://tika.apache.org/ to also support Markdown etc.
    val contentType: String? = URLConnection.guessContentTypeFromName(file.name)

    // Extend this with a mapping for other node types, e.g. zip files, and add a corresponding subclass of
    // FileSystemNode which lists its children
    return when {
        file.isDirectory -> DirectoryNode(file)
        contentType != null -> ContentReadableFileLeafNode(file, contentType)
        else -> FileLeafNode(file)
    }
}

interface ContentReadable {
    val contentType: String
    fun contentInputStream(): InputStream
}

private abstract class FileSystemNode(val file: File) : Node {
    override val label: String get() = file.name
}

private class DirectoryNode(file: File) : FileSystemNode(file) {
    init {
        if (!file.isDirectory) {
            throw Exception("Cannot create a DirectoryNode for a File which doesn't represent a directory")
        }
    }

    override fun listChildren(): List<Node> {
        // Some directories can't be listed and will return null
        return file.listFiles()?.let { arrayOfFiles ->
            arrayOfFiles.sortWith(comparator = compareBy({ !it.isDirectory }, { it.name }))
            arrayOfFiles.map { Node(it) }
        }
            ?: emptyList()
    }
}

private open class FileLeafNode(file: File) : FileSystemNode(file) {
    init {
        if (file.isDirectory) {
            throw Exception("Cannot create a FileNode for a File which represents a directory")
        }
    }

    override fun listChildren(): List<Node> {
        return emptyList()
    }
}

private class ContentReadableFileLeafNode(file: File, override val contentType: String) : FileLeafNode(file),
    ContentReadable {
    override fun contentInputStream(): InputStream {
        return file.inputStream()
    }
}